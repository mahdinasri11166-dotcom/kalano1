package com.example.anbar.db

import com.example.anbar.*
import org.json.JSONArray
import org.json.JSONObject

object RoomMapper {

    fun toEntity(p: Product) = ProductEntity(
        p.id, p.name, p.code, p.barcode, p.unit, p.hasVariants, p.trackBatch,
        p.description, p.purchasePrice, p.salePrice, p.category, p.imagePath, p.minStock, p.maxStock
    )

    fun toModel(e: ProductEntity) = Product(
        e.id, e.name, e.code, e.barcode, e.unit, e.hasVariants, e.trackBatch,
        e.description, e.purchasePrice, e.salePrice, e.category, e.imagePath, e.minStock, e.maxStock
    )

    fun toEntity(c: Customer) = CustomerEntity(
        c.id, c.name, c.phone, c.address, c.creditBalance, c.loyaltyPoints,
        c.notes, c.createdAt, c.creditLimit, c.loyaltyTier
    )

    fun toModel(e: CustomerEntity) = Customer(
        e.id, e.name, e.phone, e.address, e.creditBalance, e.loyaltyPoints,
        e.notes, e.createdAt, e.creditLimit, e.loyaltyTier
    )

    fun toEntity(s: StockItem) = StockEntity(
        s.id, s.productId, s.variantId, s.warehouseId, s.quantity,
        s.batchNumber, s.expiryDate, s.unitCost
    )

    fun toModel(e: StockEntity) = StockItem(
        e.id, e.productId, e.variantId, e.warehouseId, e.quantity,
        e.batchNumber, e.expiryDate, e.unitCost
    )

    fun toEntity(w: Warehouse) = WarehouseEntity(w.id, w.name, w.type, w.isDefault)
    fun toModel(e: WarehouseEntity) = Warehouse(e.id, e.name, e.type, e.isDefault)

    fun itemsToJson(items: List<SaleItem>): String {
        val arr = JSONArray()
        items.forEach { it ->
            arr.put(JSONObject().apply {
                put("productId", it.productId)
                put("variantId", it.variantId)
                put("productName", it.productName)
                put("variantName", it.variantName)
                put("unit", it.unit)
                put("quantity", it.quantity)
                put("unitPrice", it.unitPrice)
                put("discountPercent", it.discountPercent)
                put("lineTotal", it.lineTotal)
            })
        }
        return arr.toString()
    }

    fun itemsFromJson(json: String): List<SaleItem> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val it = arr.getJSONObject(i)
                SaleItem(
                    productId = it.getLong("productId"),
                    variantId = if (it.isNull("variantId")) null else it.getLong("variantId"),
                    productName = it.getString("productName"),
                    variantName = it.optString("variantName", ""),
                    unit = it.optString("unit", "عدد"),
                    quantity = it.getDouble("quantity"),
                    unitPrice = it.getDouble("unitPrice"),
                    discountPercent = it.optDouble("discountPercent", 0.0),
                    lineTotal = it.optDouble("lineTotal", 0.0)
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun toEntity(s: Sale) = SaleEntity(
        s.id, s.invoiceNumber, s.customerId, s.customerName, itemsToJson(s.items),
        s.subtotal, s.discountAmount, s.discountPercent, s.vatPercent, s.vatAmount,
        s.total, s.paidAmount, s.isCredit, s.paymentMethod, s.note, s.warehouseId,
        s.timestamp, s.status, s.cashPaid, s.cardPaid, s.creditPaid, s.originalSaleId, s.cashierName
    )

    fun toModel(e: SaleEntity) = Sale(
        e.id, e.invoiceNumber, e.customerId, e.customerName, itemsFromJson(e.itemsJson),
        e.subtotal, e.discountAmount, e.discountPercent, e.vatPercent, e.vatAmount,
        e.total, e.paidAmount, e.isCredit, e.paymentMethod, e.note, e.warehouseId,
        e.timestamp, e.status, e.cashPaid, e.cardPaid, e.creditPaid, e.originalSaleId, e.cashierName
    )
}
