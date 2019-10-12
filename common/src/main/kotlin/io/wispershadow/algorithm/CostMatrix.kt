package io.wispershadow.algorithm

class CostMatrix {
    private lateinit var costValueHolder: Array<Array<Double>>
    private lateinit var rowHeadersMetadata: Array<HeaderMetadata>
    private lateinit var colHeadersMetadata: Array<HeaderMetadata>

    fun getRowValues(rowIndex: Int): Array<Double> {
        return costValueHolder[rowIndex]
    }

    fun getColumnValues(colIndex: Int): Array<Double> {
        return costValueHolder.map { curRow ->
            curRow[colIndex]
        }.toTypedArray()
    }

    fun load() {

    }


    fun computeAndExtractRowMinimum(rowIndex: Int): HeaderMetadata {
        val minRowValWithInd = costValueHolder[rowIndex].withIndex().minBy { indexedValue -> indexedValue.value }
        minRowValWithInd?.let {
            rowHeadersMetadata[rowIndex].minIndex = it.index
            rowHeadersMetadata[rowIndex].minValue = it.value
            var zeroCount = 0
            costValueHolder[rowIndex].forEachIndexed {colIndex, value ->
                val diff = value - minRowValWithInd.value
                costValueHolder[rowIndex][colIndex] = diff
                if (diff == 0.0) {
                    zeroCount ++
                }
            }
            rowHeadersMetadata[rowIndex].zeroCount = zeroCount
        }
        return rowHeadersMetadata[rowIndex]
    }

    fun computeAndExtractColMinimum(colIndex: Int): HeaderMetadata {
        val minColValWithInd = costValueHolder.mapIndexed {rowIndex, curRow ->
            rowIndex to curRow[colIndex]
        }.toList().minBy { pair -> pair.second }
        minColValWithInd?.let {
            colHeadersMetadata[colIndex].minIndex = it.first
            colHeadersMetadata[colIndex].minValue = it.second
            var zeroCount = 0
            costValueHolder.forEachIndexed {rowIndex, value ->

            }
        }
        return colHeadersMetadata[colIndex]
    }


    fun isMatrixsAssignable() {

    }

}

class HeaderMetadata {
    var minIndex = -1
    var minValue: Double = 0.0
    var zeroCount = 0
}