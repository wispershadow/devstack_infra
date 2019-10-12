package io.wispershadow.infra.schema

class Schema {
    lateinit var name: String
    var version: Long = 1
    val columns = mutableListOf<Column>()

    fun addColumn(column: Column) {
        columns.add(column)
    }

    companion object {
        @JvmStatic
        fun of(name: String, version: Long, columns: List<Column>): Schema {
            val schema = Schema().apply {
                this.name = name
                this.version = version
            }
            columns.forEach { column ->
                schema.addColumn(column)
            }
            return schema
        }
    }
}

class Column {
    lateinit var name: String
    var key: Boolean = false
    var nullable: Boolean = false
    lateinit var type: ColumnType

    companion object {
        @JvmStatic
        fun of(name: String, type: ColumnType, key: Boolean = false, nullable: Boolean = true): Column {
            return Column().apply {
                this.name = name
                this.key = key
                this.nullable = nullable
                this.type = type
            }
        }
    }
}

enum class ColumnType {
    STRING,
    INT,
    LONG,
    DECIMAL,
    DATE
}