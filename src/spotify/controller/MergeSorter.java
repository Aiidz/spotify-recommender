package spotify.controller;

import java.util.Comparator;
import java.util.List;

/**
 * =============================================================================
 * MergeSorter — MERGE SORT IMPLEMENTATION FROM SCRATCH
 * =============================================================================
 * A generic, stable Merge Sort implementation that sorts any List of type T
 * using a provided Comparator.
 *
 * COMPLEXITY ANALYSIS:
 * ───────────────────
 * Time Complexity:  O(n log n) — ALWAYS (best, average, and worst case)
 * Space Complexity: O(n) — auxiliary array for merging
 *
 * RECURRENCE RELATION:
 *   T(n) = 2·T(n/2) + O(n)
 *
 * Where:
 *   2·T(n/2)  = two recursive calls on halves of the input
 *   O(n)      = the merge step that combines two sorted halves
 *
 * EXPANSION:
 *   T(n) = 2·T(n/2) + n
 *        = 2·[2·T(n/4) + n/2] + n
 *        = 4·T(n/4) + 2n
 *        = 4·[2·T(n/8) + n/4] + 2n
 *        = 8·T(n/8) + 3n
 *        = 2^k · T(n/2^k) + k·n
 *
 *   When n/2^k = 1 → k = log₂(n):
 *   T(n) = n · T(1) + n · log₂(n)
 *        = n + n·log₂(n)
 *        = O(n log n)  ■
 *
 * STABILITY:
 *   This implementation is STABLE — when two elements compare as equal,
 *   their original relative order is preserved. This matters when the user
 *   sorts by Vibe Score and two tracks have the same score.
 * =============================================================================
 */
public class MergeSorter {

    /**
     * Sorts the given list in-place using Merge Sort.
     *
     * <b>How it works (Divide and Conquer):</b>
     * <ol>
     *   <li><b>DIVIDE</b> — Split the list into two halves. Each half
     *       is roughly n/2 elements. This is O(1) work per call.</li>
     *   <li><b>CONQUER</b> — Recursively sort each half.
     *       This gives us the recurrence: 2·T(n/2).</li>
     *   <li><b>COMBINE</b> — Merge the two sorted halves back together
     *       by comparing elements one-by-one with the comparator.
     *       This is O(n) because every element is examined exactly once.</li>
     * </ol>
     *
     * Total per level: O(n). Number of levels: log₂(n).
     * Overall: O(n log n).
     *
     * @param list       The list to sort (modified in-place)
     * @param comparator Defines the sort order (ascending or descending)
     * @param <T>        Any type that can be compared via the comparator
     */
    public static <T> void sort(List<T> list, Comparator<T> comparator) {
        // Base case: lists of size 0 or 1 are already sorted
        if (list == null || list.size() <= 1) {
            return;
        }

        // Create a temporary array for the merge process
        // Space complexity: O(n) — one auxiliary array of size n
        @SuppressWarnings("unchecked")
        T[] temp = (T[]) new Object[list.size()];

        // Begin the recursive divide-and-conquer
        // Initial call sorts the entire range: [0, list.size()-1]
        mergeSortRecursive(list, temp, 0, list.size() - 1, comparator);
    }

    /**
     * Recursively divides the list into halves until sublists of size 1
     * are reached, then merges them back together in sorted order.
     *
     * Time complexity at this level: O(right - left + 1) for the merge.
     * Total across all levels: O(n log n).
     *
     * @param list       The list being sorted
     * @param temp       Temporary array for merging (reused to avoid reallocation)
     * @param left       Left boundary index (inclusive)
     * @param right      Right boundary index (inclusive)
     * @param comparator Comparison function for ordering
     * @param <T>        The element type
     */
    private static <T> void mergeSortRecursive(List<T> list, T[] temp,
                                                int left, int right,
                                                Comparator<T> comparator) {
        // BASE CASE: A single element (or empty range) is already sorted.
        // This is the termination of our recursion — O(1) work.
        if (left >= right) {
            return;
        }

        // DIVIDE STEP: Find the midpoint to split into two halves.
        // Using unsigned right shift to avoid integer overflow on very large arrays.
        // This is O(1) — simple arithmetic.
        int mid = (left + right) >>> 1;

        // CONQUER STEP: Recursively sort the left half [left, mid].
        // This call handles T(n/2) elements.
        // Over the full recursion tree, there are log₂(n) levels,
        // and each level processes all n elements total.
        mergeSortRecursive(list, temp, left, mid, comparator);

        // CONQUER STEP: Recursively sort the right half [mid+1, right].
        // Same complexity as the left half: T(n/2).
        mergeSortRecursive(list, temp, mid + 1, right, comparator);

        // COMBINE STEP: Merge the two sorted halves [left, mid] and [mid+1, right].
        // This scans each element exactly once — O(n) at this level.
        // The merge is what guarantees O(n log n) overall:
        //   n elements × log₂(n) levels = O(n log n)
        merge(list, temp, left, mid, right, comparator);
    }

    /**
     * Merges two adjacent sorted subarrays: [left, mid] and [mid+1, right].
     *
     * This is the heart of Merge Sort's O(n log n) guarantee.
     * The merge step runs in O(right - left + 1) time because:
     *   - Each element from both halves is compared exactly once
     *   - Each element is written to the temp array exactly once
     *   - Each element is copied back to the original list exactly once
     *
     * STABILITY: When comparator.compare(a, b) <= 0, we pick from the left half
     * first, preserving the original order of equal elements.
     *
     * @param list       The list being sorted
     * @param temp       Temporary storage for the merged result
     * @param left       Start of the left subarray
     * @param mid        End of the left subarray / boundary
     * @param right      End of the right subarray
     * @param comparator Comparison function for ordering
     * @param <T>        The element type
     */
    private static <T> void merge(List<T> list, T[] temp, int left, int mid,
                                   int right, Comparator<T> comparator) {

        // Pointers for the left half, right half, and temp array
        int i = left;      // Pointer for left subarray [left, mid]
        int j = mid + 1;   // Pointer for right subarray [mid+1, right]
        int k = left;      // Pointer for temp array

        // Compare elements from both halves and copy the smaller one to temp.
        // Each iteration processes exactly one element — O(right - left + 1) total.
        while (i <= mid && j <= right) {
            // STABLE comparison: use <= to prefer the left element on ties.
            // This preserves the original order of equal elements.
            if (comparator.compare(list.get(i), list.get(j)) <= 0) {
                temp[k++] = list.get(i++);
            } else {
                temp[k++] = list.get(j++);
            }
        }

        // Copy any remaining elements from the left half.
        // These are already in sorted order relative to each other.
        // At most mid - i + 1 iterations — bounded by O(n).
        while (i <= mid) {
            temp[k++] = list.get(i++);
        }

        // Copy any remaining elements from the right half.
        // Same reasoning — bounded by O(n).
        while (j <= right) {
            temp[k++] = list.get(j++);
        }

        // Copy the merged result back into the original list.
        // This is O(right - left + 1) — each element written back once.
        for (int idx = left; idx <= right; idx++) {
            list.set(idx, temp[idx]);
        }

        // TOTAL MERGE COST: O(right - left + 1) = O(n) at this level
        // Combined with log₂(n) recursion depth → O(n log n) overall.
    }
}
