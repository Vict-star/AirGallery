package cn.edu.scut.airgallery.ui.about;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AboutViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AboutViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("2020字节跳动玩转客户端\n" +
                "第三组项目\n" +
                "zcf yzh lyz yjh\n");
    }

    public LiveData<String> getText() {
        return mText;
    }
}