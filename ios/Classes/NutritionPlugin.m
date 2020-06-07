#import "NutritionPlugin.h"
#if __has_include(<nutrition/nutrition-Swift.h>)
#import <nutrition/nutrition-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "nutrition-Swift.h"
#endif

@implementation NutritionPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftNutritionPlugin registerWithRegistrar:registrar];
}
@end
