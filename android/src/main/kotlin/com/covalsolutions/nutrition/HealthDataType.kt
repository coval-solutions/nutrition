package com.covalsolutions.nutrition

import com.google.android.gms.fitness.data.DataType

enum class HealthDataType(val value: DataType) {
    TOTAL_FAT(DataType.TYPE_NUTRITION),
    CALCIUM(DataType.TYPE_NUTRITION),
    SUGAR(DataType.TYPE_NUTRITION),
    FIBER(DataType.TYPE_NUTRITION),
    IRON(DataType.TYPE_NUTRITION),
    POTASSIUM(DataType.TYPE_NUTRITION),
    SODIUM(DataType.TYPE_NUTRITION),
    VITAMIN_A(DataType.TYPE_NUTRITION),
    VITAMIN_C(DataType.TYPE_NUTRITION),
    PROTEIN(DataType.TYPE_NUTRITION),
    CHOLESTEROL(DataType.TYPE_NUTRITION),
    TOTAL_CARBS(DataType.TYPE_NUTRITION),
    AGGREGATE_NUTRITION_SUMMARY(DataType.AGGREGATE_NUTRITION_SUMMARY);

    companion object {
        fun keyToHealthDataType(type: String): HealthDataType {
            return when (type) {
                "totalFat" -> TOTAL_FAT
                "calcium" -> CALCIUM
                "sugar" -> SUGAR
                "fiber" -> FIBER
                "iron" -> IRON
                "potassium" -> POTASSIUM
                "sodium" -> SODIUM
                "vitaminA" -> VITAMIN_A
                "vitaminC" -> VITAMIN_C
                "protein" -> PROTEIN
                "cholesterol" -> CHOLESTEROL
                "totalCarbs" -> TOTAL_CARBS
                "aggregateNutritionSummary" -> AGGREGATE_NUTRITION_SUMMARY
                else -> throw IllegalArgumentException("Unsupported dataType: $type")
            }
        }
    }

}