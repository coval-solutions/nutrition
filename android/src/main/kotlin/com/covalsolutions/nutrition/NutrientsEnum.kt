package com.covalsolutions.nutrition

import com.google.android.gms.fitness.data.Field

enum class NutrientsEnum(nutrientField: String) {
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
    TOTAL_CARBS(Field.NUTRIENT_TOTAL_CARBS),
}