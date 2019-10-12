package io.wispershadow.infra.schema

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.Assert
import org.junit.Test
import org.springframework.beans.BeanWrapperImpl
import java.math.BigDecimal

class SchemaClassGeneratorTest {
    @Test
    fun testSchemaClassGeneration() {
        val schemaClassGenerator = SchemaClassGenerator()
        val schema = Schema().apply {
            this.name = "testObject"
            this.version = 1L
        }
        schema.addColumn(Column.of("paymentId", ColumnType.STRING, true))
        schema.addColumn(Column.of("payerAmountUsd", ColumnType.DECIMAL, true))
        schema.addColumn(Column.of("payerCountry", ColumnType.STRING, true))
        schema.addColumn(Column.of("payerAge", ColumnType.INT, true))

        val schemaClass = schemaClassGenerator.getSchemaClass(schema, SchemaClassGenerator::class.java.classLoader)
        Assert.assertEquals(schemaClass.name, "TestObjectV1")
        Assert.assertEquals(schemaClass.declaredMethods.size, 8)
        val paymentData = "{\"paymentId\": \"1211212121\", \"payerAmountUsd\": 311.123, \"payerCountry\": \"US\", \"payerAge\": 10}"
        val objectMapper = ObjectMapper()
        val data = objectMapper.readValue(paymentData, schemaClass)
        Assert.assertEquals(getByPropertyName(data, "paymentId"), "1211212121")
        Assert.assertEquals(getByPropertyName(data, "payerAmountUsd"), BigDecimal("311.123"))
        Assert.assertEquals(getByPropertyName(data, "payerCountry"), "US")
        Assert.assertEquals(getByPropertyName(data, "payerAge"), 10)

        val sameSchemaClass = schemaClassGenerator.getSchemaClass(schema, SchemaClassGenerator::class.java.classLoader)
        Assert.assertEquals(sameSchemaClass.name, "TestObjectV1")
    }

    private fun getByPropertyName(data: Any, propertyName: String): Any? {
        return BeanWrapperImpl(data).getPropertyValue(propertyName)
    }
}