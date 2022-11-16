# ImagePicker
Android Library for the Images Selector.

###  build.gradle

    implementation 'com.github.girimystr1997:ImagePicker:v1.1.4'

### settings.gradle

    repositories {
        maven { url "https://jitpack.io" }
    }


### Kotlin

ImagePickerBuilder:
                
    ImagePickerBuilder.instance
                .showImages(true)
                .start(this,resultLauncher)

Callback:

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.getBundleExtra("FilePath").let { it1 ->
                    it1?.getParcelable<FileModel>("FilePath").let { fileModel ->
                        //do with filemodel
                    }
                }
            }
        }

### Java

FilePickerBuilder:

    new ImagePickerBuilder()
                .showImages(true)
                .start(this,activityResultLauncher);
                
Callback:

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK){
                    Bundle bundle = result.getData().getBundleExtra("FilePath");
                    FileModel fileModel = bundle.getParcelable("FilePath");
                    if (fileModel != null) {
                        //do  with filemodel
                    }
                }
            }
        });


