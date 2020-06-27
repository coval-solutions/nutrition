#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html.
# Run `pod lib lint nutrition.podspec' to validate before publishing.
#
Pod::Spec.new do |s|
  s.name             = 'nutrition'
  s.version          = '1.0.0'
  s.summary          = 'Flutter plugin which allows you to acess Google Fit and iOS Health nutrition data.'
  s.description      = <<-DESC
Flutter plugin which allows you to acess Google Fit and iOS Health nutrition data.
                       DESC
  s.homepage         = 'https://covalsolutions.com/'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Coval Solutions Ltd' => 'support@covalsolutions.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.dependency 'Flutter'
  s.platform = :ios, '8.0'

  # Flutter.framework does not contain a i386 slice. Only x86_64 simulators are supported.
  s.pod_target_xcconfig = { 'DEFINES_MODULE' => 'YES', 'VALID_ARCHS[sdk=iphonesimulator*]' => 'x86_64' }
  s.swift_version = '5.0'
end
