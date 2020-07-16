<h1 align="center">Nutrition for Flutter</h1>
<h3 align="center">Google Fit and Apple Health Wrapper</h3>
<p align="center">
  <img src="https://api.codemagic.io/apps/5f0df9acc52b8c001c0c9224/5f0df9acc52b8c001c0c9223/status_badge.svg" />
  <a href="https://pub.dev/packages/nutrition">
    <img alt="Pub Version" src="https://img.shields.io/pub/v/nutrition">
  </a>
  <a href="https://raw.githubusercontent.com/coval-solutions/weight-slider/master/LICENSE">
    <img alt="License: MIT" src="https://img.shields.io/badge/license-MIT-yellow.svg" target="_blank" />
  </a>
</p>

## ✨ Demo

See `example`

## 🚀 Usage

| Supported Data Types |
| -------------------- |
| Total Fat            |
| Calcium              |
| Sugar                |
| Fiber                |
| Iron                 |
| Potassium            |
| Sodium               |
| Vitamin A            |
| Vitamin C            |
| Protein              |
| Cholesterol          |
| Total Carbohydrates  |

### ⚠️ Request Permission

```dart
bool hasPermission = false;
await Nutrition.requestPermission().then((value) => setState(() {
  hasPermission = value;
}));
```

### 🍎 Get Data

_Note for Android users you must have Google Sign In setup with your Flutter app_

```dart
DateTime endDate = DateTime.now();
DateTime startDate = DateTime.now().subtract(Duration(days: 7));
Nutrition.getData(startDate, endDate);
```

### 🍌 Add Data

_Note for Android users you must have Google Sign In setup with your Flutter app_

```dart
DateTime date = DateTime.now();
Map<NutritionEnum, double> nutrients = {
  NutritionEnum.FIBRE: 10,
  NutritionEnum.FAT: 5,
  NutritionEnum.PROTEIN: 2.5,
  NutritionEnum.CARBOHYDRATES: 100
};

// Pass a Map<NutritionEnum, double> and a date
Nutrition.addData(nutrients, date);
```

## 📝 License

This project is [MIT](https://raw.githubusercontent.com/coval-solutions/nutrition/master/LICENSE) licensed.
