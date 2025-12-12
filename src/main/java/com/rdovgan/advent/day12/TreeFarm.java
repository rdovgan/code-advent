package com.rdovgan.advent.day12;

import com.rdovgan.advent.util.ResourceData;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TreeFarm {

	private TreeFarm() {}

	// максимальна кількість предметів для повного backtracking (точного)
	private static final int EXACT_BACKTRACK_LIMIT = 12;
	// таймаут на спробу розміщення одного регіону (в мілісекундах)
	private static final long REGION_TIMEOUT_MS = 6_000L;

	/**
	 * Головний метод: повертає кількість регіонів, у які можна вмістити всі подарунки.
	 */
	public static int countFittableRegions(List<String> inputLines) {
		if (inputLines == null || inputLines.isEmpty()) return 0;

		// Парсимо форму та регіони
		LinkedHashMap<Integer, List<String>> shapesRaw = new LinkedHashMap<>();
		List<String> regionLines = new ArrayList<>();

		Pattern shapeHeader = Pattern.compile("^\\s*(\\d+)\\s*:\\s*$");
		Pattern regionHeader = Pattern.compile("^\\s*(\\d+)\\s*x\\s*(\\d+)\\s*:\\s*(.*)$");

		boolean readingShapes = true;
		int currentShape = -1;
		for (String raw : inputLines) {
			String line = raw == null ? "" : raw;
			Matcher mh = shapeHeader.matcher(line);
			Matcher mr = regionHeader.matcher(line);
			// при зустрічі першого regionHeader переключаємося
			if (readingShapes && mr.matches()) {
				readingShapes = false;
			}
			if (readingShapes) {
				if (mh.matches()) {
					currentShape = Integer.parseInt(mh.group(1));
					shapesRaw.put(currentShape, new ArrayList<>());
				} else {
					if (currentShape >= 0 && !line.trim().isEmpty()) {
						shapesRaw.get(currentShape).add(line.trim());
					}
				}
			} else {
				if (!line.trim().isEmpty()) regionLines.add(line.trim());
			}
		}

		// Якщо не знайшли регіонів — спробуємо ще раз (на випадок відсутнього clear boundary)
		if (regionLines.isEmpty()) {
			boolean switchNow = false;
			currentShape = -1;
			shapesRaw.clear();
			for (String raw : inputLines) {
				String line = raw == null ? "" : raw;
				Matcher mr = regionHeader.matcher(line);
				Matcher mh = shapeHeader.matcher(line);
				if (mr.matches()) switchNow = true;
				if (!switchNow) {
					if (mh.matches()) {
						currentShape = Integer.parseInt(mh.group(1));
						shapesRaw.put(currentShape, new ArrayList<>());
					} else {
						if (currentShape >= 0 && !line.trim().isEmpty()) shapesRaw.get(currentShape).add(line.trim());
					}
				} else {
					if (!line.trim().isEmpty()) regionLines.add(line.trim());
				}
			}
		}

		// Якщо немає форм — нічого робити
		if (shapesRaw.isEmpty()) return 0;

		// Normalize shape indices order
		List<Integer> indices = new ArrayList<>(shapesRaw.keySet());
		Collections.sort(indices);

		// Сформувати для кожної форми список унікальних варіантів (flattened coords)
		List<List<int[]>> shapeVariants = new ArrayList<>();
		for (int idx : indices) {
			List<String> rows = shapesRaw.get(idx);
			int[][] coords = parseShape(rows);
			List<int[][]> transforms = generateTransforms(coords);
			LinkedHashMap<String, int[][]> uniq = new LinkedHashMap<>();
			for (int[][] t : transforms) uniq.put(normalizeKey(t), t);
			List<int[]> variants = new ArrayList<>();
			for (int[][] t : uniq.values()) {
				int[] flat = new int[t.length * 2];
				for (int i = 0; i < t.length; i++) { flat[2*i] = t[i][0]; flat[2*i+1] = t[i][1]; }
				variants.add(flat);
			}
			shapeVariants.add(variants);
		}

		int successCount = 0;
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			for (String rline : regionLines) {
				final Matcher mr = regionHeader.matcher(rline);
				if (!mr.matches()) continue;
				final int W = Integer.parseInt(mr.group(1));
				final int H = Integer.parseInt(mr.group(2));
				final String rest = mr.group(3).trim();

				// якщо немає предметів — вміщається
				if (rest.isEmpty()) { successCount++; continue; }

				String[] parts = rest.split("\\s+");
				List<Integer> counts = new ArrayList<>();
				for (String p : parts) {
					if (p.trim().isEmpty()) continue;
					try { counts.add(Integer.parseInt(p.trim())); } catch (NumberFormatException ex) { counts.add(0); }
				}
				// підгоняємо під кількість форм
				while (counts.size() < shapeVariants.size()) counts.add(0);
				if (counts.size() > shapeVariants.size()) counts = counts.subList(0, shapeVariants.size());

				// побудувати список предметів
				List<Integer> pieces = new ArrayList<>();
				for (int s = 0; s < counts.size(); s++) {
					for (int k = 0; k < counts.get(s); k++) pieces.add(s);
				}

				// швидка перевірка площ
				long totalCells = 0;
				boolean invalidShape = false;
				for (int pi : pieces) {
					List<int[]> vars = shapeVariants.get(pi);
					if (vars.isEmpty()) { invalidShape = true; break; }
					totalCells += (vars.get(0).length / 2);
				}
				if (invalidShape) continue;
				if (totalCells > (long) W * H) continue;

				// побудувати Piece list
				List<Piece> pieceList = new ArrayList<>(pieces.size());
				for (int sidx : pieces) {
					List<int[]> vars = shapeVariants.get(sidx);
					int area = vars.isEmpty() ? 0 : vars.get(0).length/2;
					pieceList.add(new Piece(sidx, area, vars));
				}
				// сортування: найбільші спочатку
				pieceList.sort(Comparator.comparingInt((Piece p) -> -p.area).thenComparingInt(p -> p.variants.size()));

				// якщо предметів мало — точний backtracking (з таймаутом)
				boolean fit = false;
				if (pieceList.size() <= EXACT_BACKTRACK_LIMIT) {
					final boolean[][] grid = new boolean[H][W];
					Callable<Boolean> task = () -> packPieces(grid, W, H, pieceList, 0);
					Future<Boolean> future = executor.submit(task);
					try {
						fit = future.get(REGION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
					} catch (TimeoutException te) {
						future.cancel(true);
						fit = false;
					} catch (Exception ex) {
						future.cancel(true);
						fit = false;
					}
				} else {
					// Для великих випадків — спробуємо швидкий greedy; якщо вдасться — приймаємо як fit.
					boolean[][] grid = new boolean[H][W];
					fit = greedyPlace(grid, W, H, pieceList);
				}

				if (fit) successCount++;
			}
		} finally {
			executor.shutdownNow();
		}

		return successCount;
	}

	/* ---------- greedy placement (heuristic, fast) ---------- */
	private static boolean greedyPlace(boolean[][] grid, int W, int H, List<Piece> pieces) {
		// простий greedy: для кожного piece в порядку pieces:
		//   пробуємо кожен варіант і кожну позицію (scan top-left->bottom-right),
		//   якщо вдається помістити — фіксуємо і переходимо далі.
		for (Piece p : pieces) {
			boolean placed = false;
			for (int[] var : p.variants) {
				int coords = var.length / 2;
				// precompute variant bounding width/height
				int maxX = 0, maxY = 0;
				for (int k = 0; k < coords; k++) {
					maxX = Math.max(maxX, var[2*k]);
					maxY = Math.max(maxY, var[2*k + 1]);
				}
				for (int y = 0; y + maxY < H && !placed; y++) {
					for (int x = 0; x + maxX < W && !placed; x++) {
						boolean ok = true;
						for (int k = 0; k < coords; k++) {
							int cx = x + var[2*k];
							int cy = y + var[2*k + 1];
							if (grid[cy][cx]) { ok = false; break; }
						}
						if (ok) {
							for (int k = 0; k < coords; k++) {
								int cx = x + var[2*k];
								int cy = y + var[2*k + 1];
								grid[cy][cx] = true;
							}
							placed = true;
						}
					}
				}
				if (placed) break;
			}
			if (!placed) return false;
		}
		return true;
	}

	/* ---------- backtracking placement (exact) ---------- */

	private static boolean packPieces(boolean[][] grid, int W, int H, List<Piece> pieces, int pi) {
		// cooperative thread cancel check
		if (Thread.currentThread().isInterrupted()) return false;

		if (pi >= pieces.size()) return true;
		Piece piece = pieces.get(pi);

		// find first free cell
		int startY = -1, startX = -1;
		outer:
		for (int y = 0; y < H; y++) {
			for (int x = 0; x < W; x++) {
				if (!grid[y][x]) { startY = y; startX = x; break outer; }
			}
		}
		if (startY == -1) {
			for (int i = pi; i < pieces.size(); i++) if (pieces.get(i).area > 0) return false;
			return true;
		}

		for (int[] var : piece.variants) {
			int coordsCount = var.length / 2;
			for (int anchor = 0; anchor < coordsCount; anchor++) {
				int anchorX = var[2*anchor];
				int anchorY = var[2*anchor + 1];
				int baseX = startX - anchorX;
				int baseY = startY - anchorY;
				boolean ok = true;
				for (int k = 0; k < coordsCount; k++) {
					int cx = baseX + var[2*k];
					int cy = baseY + var[2*k + 1];
					if (cx < 0 || cx >= W || cy < 0 || cy >= H) { ok = false; break; }
					if (grid[cy][cx]) { ok = false; break; }
				}
				if (!ok) continue;
				for (int k = 0; k < coordsCount; k++) {
					int cx = baseX + var[2*k];
					int cy = baseY + var[2*k + 1];
					grid[cy][cx] = true;
				}
				if (packPieces(grid, W, H, pieces, pi + 1)) return true;
				for (int k = 0; k < coordsCount; k++) {
					int cx = baseX + var[2*k];
					int cy = baseY + var[2*k + 1];
					grid[cy][cx] = false;
				}
			}
		}

		// allow skipping only for zero-area
		if (piece.area == 0) return packPieces(grid, W, H, pieces, pi + 1);
		return false;
	}

	/* ---------- shape utilities ---------- */

	// parse visual shape to normalized coordinate list
	private static int[][] parseShape(List<String> rows) {
		if (rows == null || rows.isEmpty()) return new int[0][0];
		List<int[]> coords = new ArrayList<>();
		for (int y = 0; y < rows.size(); y++) {
			String row = rows.get(y);
			for (int x = 0; x < row.length(); x++) {
				if (row.charAt(x) == '#') coords.add(new int[]{x, y});
			}
		}
		return normalize(coords);
	}

	private static int[][] normalize(List<int[]> coords) {
		if (coords == null || coords.isEmpty()) return new int[0][0];
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
		for (int[] p : coords) { minX = Math.min(minX, p[0]); minY = Math.min(minY, p[1]); }
		int[][] out = new int[coords.size()][2];
		for (int i = 0; i < coords.size(); i++) out[i] = new int[]{ coords.get(i)[0] - minX, coords.get(i)[1] - minY };
		Arrays.sort(out, Comparator.comparingInt((int[] a) -> a[1]).thenComparingInt(a -> a[0]));
		return out;
	}

	// generate unique transforms (rotations + flip)
	private static List<int[][]> generateTransforms(int[][] coords) {
		List<int[][]> res = new ArrayList<>();
		if (coords == null || coords.length == 0) return res;
		List<int[]> base = new ArrayList<>();
		for (int[] p : coords) base.add(new int[]{p[0], p[1]});
		for (int flip = 0; flip < 2; flip++) {
			for (int rot = 0; rot < 4; rot++) {
				List<int[]> transformed = new ArrayList<>();
				for (int[] p : base) {
					int x = p[0], y = p[1];
					if (flip == 1) x = -x;
					int rx = x, ry = y;
					for (int r = 0; r < rot; r++) {
						int nx = ry;
						int ny = -rx;
						rx = nx; ry = ny;
					}
					transformed.add(new int[]{rx, ry});
				}
				int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
				for (int[] p : transformed) { minX = Math.min(minX, p[0]); minY = Math.min(minY, p[1]); }
				for (int[] p : transformed) { p[0] -= minX; p[1] -= minY; }
				int[][] arr = new int[transformed.size()][2];
				for (int i = 0; i < transformed.size(); i++) arr[i] = transformed.get(i).clone();
				Arrays.sort(arr, Comparator.comparingInt((int[] a) -> a[1]).thenComparingInt(a -> a[0]));
				res.add(arr);
			}
		}
		LinkedHashMap<String, int[][]> uniq = new LinkedHashMap<>();
		for (int[][] a : res) uniq.put(normalizeKey(a), a);
		return new ArrayList<>(uniq.values());
	}

	private static String normalizeKey(int[][] coords) {
		if (coords == null || coords.length == 0) return "";
		StringBuilder sb = new StringBuilder(coords.length * 4);
		for (int[] p : coords) { sb.append(p[0]).append(',').append(p[1]).append(';'); }
		return sb.toString();
	}

	private static final class Piece {
		final int shapeIndex;
		final int area;
		final List<int[]> variants;
		Piece(int idx, int area, List<int[]> vars) { this.shapeIndex = idx; this.area = area; this.variants = vars; }
	}

	/* ---------- main for quick run ---------- */
	public static void main(String[] args) {
		List<String> lines = new ResourceData().loadFromResource("data12.csv");
		System.out.println(countFittableRegions(lines));
	}
}
