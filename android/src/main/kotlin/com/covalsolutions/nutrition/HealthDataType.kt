package com.covalsolutions.nutrition

import com.google.android.gms.fitness.data.Field

enum class HealthDataType(val value: String) {
    TOTAL_FAT(Field.NUTRIENT_TOTAL_FAT),
    CALCIUM(Field.NUTRIENT_CALCIUM),
    SUGAR(Field.NUTRIENT_SUGAR),
    FIBER(Field.NUTRIENT_DIETARY_FIBER),
    IRON(Field.NUTRIENT_IRON),
    POTASSIUM(Field.NUTRIENT_POTASSIUM),
    SODIUM(Field.NUTRIENT_SODIUM),
    VITAMIN_A(Field.NUTRIENT_VITAMIN_A),
    VITAMIN_C(Field.NUTRIENT_VITAMIN_C),
    PROTEIN(Field.NUTRIENT_PROTEIN),
    CHOLESTEROL(Field.NUTRIENT_CHOLESTEROL),
    TOTAL_CARBS(Field.NUTRIENT_TOTAL_CARBS);

    companion object {
        private const val TOTAL_FAT_KEY = "totalFat"
        private const val CALCIUM_KEY = "calcium"
        private const val SUGAR_KEY = "sugar"
        private const val FIBER_KEY = "fiber"
        private const val IRON_KEY = "iron"
        private const val POTASSIUM_KEY = "potassium"
        private const val SODIUM_KEY = "sodium"
        private const val VITAMIN_A_KEY = "vitaminA"
        private const val VITAMIN_C_KEY = "vitaminC"
        private const val PROTEIN_KEY = "protein"
        private const val CHOLESTEROL_KEY = "cholesterol"
        private const val TOTAL_CARBS_KEY = "totalCarbs"

        fun keyToHealthDataType(type: String): HealthDataType {
            return when (type) {
                TOTAL_FAT_KEY -> TOTAL_FAT
                CALCIUM_KEY -> CALCIUM
                SUGAR_KEY -> SUGAR
                FIBER_KEY -> FIBER
                IRON_KEY -> IRON
                POTASSIUM_KEY -> POTASSIUM
                SODIUM_KEY -> SODIUM
                VITAMIN_A_KEY -> VITAMIN_A
                VITAMIN_C_KEY -> VITAMIN_C
                PROTEIN_KEY -> PROTEIN
                CHOLESTEROL_KEY -> CHOLESTEROL
                TOTAL_CARBS_KEY -> TOTAL_CARBS
                else -> throw IllegalArgumentException("Unsupported type: $type")
            }
        }

    }

}