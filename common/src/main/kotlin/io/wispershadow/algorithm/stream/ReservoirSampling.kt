package io.wispershadow.algorithm.stream

import java.security.SecureRandom

object ReservoirSampling {
    private val secureRandom = SecureRandom()
    fun <E> getSamples(currentSampleResult: SampleResult<E>, streamDataList: List<E>, samplingSize: Int): SampleResult<E>  {
        streamDataList.forEach { streamData ->
            addValue(streamData, currentSampleResult, samplingSize)
        }
        return currentSampleResult
    }

    private fun <E> addValue(curElem: E, currentSampleResult: SampleResult<E> , samplingSize: Int) {
        val currentSamples = currentSampleResult.curSamples
        if (currentSamples.size < samplingSize) {
            currentSamples.add(curElem)
        }
        else {
            var currentSize = currentSampleResult.currentSize
            currentSize ++
            val newIndex = (secureRandom.nextDouble() * (samplingSize + 1 + currentSize)).toLong()
            if (newIndex < samplingSize) {
                currentSamples[newIndex.toInt()] = curElem
            }
            currentSampleResult.currentSize = currentSize
        }
    }
}

class SampleResult<E> (
    val curSamples: MutableList<E>,
    var currentSize: Long = 0
)