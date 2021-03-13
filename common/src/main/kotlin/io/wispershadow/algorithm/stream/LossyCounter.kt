package io.wispershadow.algorithm.stream

object LossyCounter {
    fun <E> calculate(frequencyCount: FrequencyCount<E>, streamDataList: List<E>, samplingSize: Int): FrequencyCount<E> {
        val currentFrequencyCount = FrequencyCount<E>(mutableMapOf<E, Int>())
        streamDataList.forEach { streamData ->
            currentFrequencyCount.addItem(streamData)
        }
        return frequencyCount.merge(currentFrequencyCount, samplingSize)
    }
}

class FrequencyCount<E> (
    val itemCount: MutableMap<E, Int>
) {
    fun addItem(element: E) {
        addItemWithCount(element, 1)
    }

    private fun addItemWithCount(element: E, count: Int, ignoreCount1: Boolean = false) {
        val currentValue = itemCount[element]
        if (currentValue == null) {
            if (!ignoreCount1 || count > 1) {
                itemCount.put(element, count)
            }
        }
        else {
            itemCount.put(element, currentValue + count)
        }
    }


    fun merge(another: FrequencyCount<E>, topN: Int): FrequencyCount<E> {
        another.itemCount.forEach { element, count ->
            this.addItemWithCount(element, count, true)
        }
        return this
    }
}