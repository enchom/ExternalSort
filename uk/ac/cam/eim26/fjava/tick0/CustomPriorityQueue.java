package uk.ac.cam.eim26.fjava.tick0;

import java.util.List;

/**
 * A priority queue implemented using a binary heap, supporting getting the minimum value,
 * popping the minimum value and changing the value of the current root (minimum value).
 * The class is expected to have very few elements and thus a built-in priority queue would most
 * likely perform worse (due to overhead)
 */
@Deprecated
public class CustomPriorityQueue {
    private int sz;
    private int[] heap;
    private int[] values;

    private void heapify(int ind) {
        while (2 * ind + 1 < sz) {
            if (2 * ind + 2 >= sz) {
                if (values[heap[ind]] > values[heap[2 * ind + 1]]) {
                    int tmp = heap[ind];
                    heap[ind] = heap[2 * ind + 1];
                    heap[2 * ind + 1] = tmp;

                    ind = 2 * ind + 1;
                } else {
                    break;
                }
            } else {
                if (values[heap[2 * ind + 1]] < values[heap[2 * ind + 2]]) {
                    if (values[heap[2 * ind + 1]] < values[heap[ind]]) {
                        int tmp = heap[ind];
                        heap[ind] = heap[2 * ind + 1];
                        heap[2 * ind + 1] = tmp;

                        ind = 2 * ind + 1;
                    } else {
                        break;
                    }
                } else {
                    if (values[heap[2 * ind + 2]] < values[heap[ind]]) {
                        int tmp = heap[ind];
                        heap[ind] = heap[2 * ind + 2];
                        heap[2 * ind + 2] = tmp;

                        ind = 2 * ind + 2;
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public CustomPriorityQueue(int sz, List<Integer> initialValues) {
        this.sz = sz;
        heap = new int[sz];
        values = new int[sz];

        for (int i = 0; i < sz; i++) {
            heap[i] = i;
            values[i] = initialValues.get(i);
        }

        for (int i = sz / 2; i >= 0; i--) {
            heapify(i);
        }
    }

    /**
     * Returns the top element of the priority queue without removing it.
     */
    public int top() {
        return heap[0];
    }

    /**
     * Removes the top element of the priority queue.
     */
    public void popTop() {
        heap[0] = heap[sz - 1];
        sz--;

        heapify(0);
    }

    /**
     * Replaces the top element of the priority queue and reorders it accordingly
     */
    public void replaceTop(int value) {
        values[heap[0]] = value;

        heapify(0);
    }

    /**
     * Tests whether the priority queue is empty
     */
    public boolean empty() {
        return sz == 0;
    }
}
