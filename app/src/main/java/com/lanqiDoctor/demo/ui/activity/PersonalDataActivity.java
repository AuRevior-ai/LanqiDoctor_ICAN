package com.lanqiDoctor.demo.ui.activity;

import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.lanqiDoctor.demo.R;
import com.lanqiDoctor.demo.aop.SingleClick;
import com.lanqiDoctor.demo.app.AppActivity;
import com.lanqiDoctor.demo.http.api.UpdateImageApi;
import com.lanqiDoctor.demo.http.glide.GlideApp;
import com.lanqiDoctor.demo.http.model.HttpData;
import com.lanqiDoctor.demo.manager.UserStateManager;
import com.lanqiDoctor.demo.ui.dialog.AddressDialog;
import com.lanqiDoctor.demo.ui.dialog.InputDialog;
import com.hjq.http.EasyHttp;
import com.hjq.http.listener.HttpCallback;
import com.hjq.http.model.FileContentResolver;
import com.hjq.widget.layout.SettingBar;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * 个人资料页面
 * 
 * @author 蓝岐医童开发团队
 * @version 2.0
 */
public final class PersonalDataActivity extends AppActivity {

    private ViewGroup mAvatarLayout;
    private ImageView mAvatarView;
    private SettingBar mIdView;
    private SettingBar mNameView;
    private SettingBar mAddressView;

    /** 头像地址 */
    private Uri mAvatarUrl;
    
    private UserStateManager userStateManager;

    @Override
    protected int getLayoutId() {
        return R.layout.personal_data_activity;
    }

    @Override
    protected void initView() {
        mAvatarLayout = findViewById(R.id.fl_person_data_avatar);
        mAvatarView = findViewById(R.id.iv_person_data_avatar);
        mIdView = findViewById(R.id.sb_person_data_id);
        mNameView = findViewById(R.id.sb_person_data_name);
        mAddressView = findViewById(R.id.sb_person_data_address);
        setOnClickListener(mAvatarLayout, mAvatarView, mNameView, mAddressView);
    }

    @Override
    protected void initData() {
        userStateManager = UserStateManager.getInstance(this);
        
        // 加载头像
        loadUserAvatar();
        
        // 设置用户ID（邮箱MD5的前6位）
        mIdView.setRightText(userStateManager.getUserId());
        
        // 设置用户昵称
        String nickname = userStateManager.getUserNickname();
        if (!nickname.isEmpty()) {
            mNameView.setRightText(nickname);
        } else {
            // 如果没有昵称，使用邮箱作为默认显示
            String email = userStateManager.getUserEmail();
            if (!email.isEmpty()) {
                // 只显示邮箱的用户名部分
                String defaultName = email.contains("@") ? email.substring(0, email.indexOf("@")) : email;
                mNameView.setRightText(defaultName);
            } else {
                mNameView.setRightText("用户");
            }
        }
        
        // 设置地址信息
        String address = userStateManager.getFullAddress();
        mAddressView.setRightText(address);
    }
    
    /**
     * 加载用户头像
     */
    private void loadUserAvatar() {
        String avatarUrl = userStateManager.getUserAvatar();
        
        if (!avatarUrl.isEmpty()) {
            // 如果有头像URL，加载网络头像
            GlideApp.with(getActivity())
                    .load(avatarUrl)
                    .placeholder(R.drawable.avatar_placeholder_ic)
                    .error(R.drawable.avatar_placeholder_ic)
                    .transform(new MultiTransformation<>(new CenterCrop(), new CircleCrop()))
                    .into(mAvatarView);
            mAvatarUrl = Uri.parse(avatarUrl);
        } else {
            // 加载默认头像
            GlideApp.with(getActivity())
                    .load(R.drawable.avatar_placeholder_ic)
                    .placeholder(R.drawable.avatar_placeholder_ic)
                    .error(R.drawable.avatar_placeholder_ic)
                    .transform(new MultiTransformation<>(new CenterCrop(), new CircleCrop()))
                    .into(mAvatarView);
        }
    }

    @SingleClick
    @Override
    public void onClick(View view) {
        if (view == mAvatarLayout) {
            ImageSelectActivity.start(this, data -> {
                // 裁剪头像
                cropImageFile(new File(data.get(0)));
            });
        } else if (view == mAvatarView) {
            if (mAvatarUrl != null) {
                // 查看头像
                ImagePreviewActivity.start(getActivity(), mAvatarUrl.toString());
            } else {
                // 选择头像
                onClick(mAvatarLayout);
            }
        } else if (view == mNameView) {
            new InputDialog.Builder(this)
                    .setTitle("修改昵称")
                    .setContent(mNameView.getRightText())
                    .setHint("请输入昵称")
                    .setListener((dialog, content) -> {
                        if (!mNameView.getRightText().equals(content)) {
                            // 更新显示
                            mNameView.setRightText(content);
                            // 保存到UserStateManager
                            userStateManager.updateUserInfo(content, null);
                            toast("昵称更新成功");
                        }
                    })
                    .show();
        } else if (view == mAddressView) {
            // 获取当前地址信息
            String currentProvince = userStateManager.getUserProvince();
            String currentCity = userStateManager.getUserCity();
            String currentArea = userStateManager.getUserArea();
            
            new AddressDialog.Builder(this)
                    .setTitle("选择地区")
                    // 设置当前省份
                    .setProvince(currentProvince)
                    // 设置当前城市（必须要先设置默认省份）
                    .setCity(currentCity)
                    // 设置当前区域
                    .setListener((dialog, province, city, area) -> {
                        String address = province + city + area;
                        if (!mAddressView.getRightText().equals(address)) {
                            // 更新显示
                            mAddressView.setRightText(address);
                            // 保存到UserStateManager
                            userStateManager.saveAddressInfo(province, city, area);
                            toast("地址更新成功");
                        }
                    })
                    .show();
        }
    }

    /**
     * 裁剪图片
     */
    private void cropImageFile(File sourceFile) {
        ImageCropActivity.start(this, sourceFile, 1, 1, new ImageCropActivity.OnCropListener() {

            @Override
            public void onSucceed(Uri fileUri, String fileName) {
                File outputFile;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    outputFile = new FileContentResolver(getActivity(), fileUri, fileName);
                } else {
                    try {
                        outputFile = new File(new URI(fileUri.toString()));
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                        outputFile = new File(fileUri.toString());
                    }
                }
                updateCropImage(outputFile, true);
            }

            @Override
            public void onError(String details) {
                // 没有的话就不裁剪，直接上传原图片
                // 但是这种情况极其少见，可以忽略不计
                updateCropImage(sourceFile, false);
            }
        });
    }

    /**
     * 上传裁剪后的图片
     */
    private void updateCropImage(File file, boolean deleteFile) {
        // 开发阶段直接使用本地文件，不上传服务器
        if (true) {
            if (file instanceof FileContentResolver) {
                mAvatarUrl = ((FileContentResolver) file).getContentUri();
            } else {
                mAvatarUrl = Uri.fromFile(file);
            }
            
            // 更新头像显示
            GlideApp.with(getActivity())
                    .load(mAvatarUrl)
                    .transform(new MultiTransformation<>(new CenterCrop(), new CircleCrop()))
                    .into(mAvatarView);
            
            // 保存头像URL到UserStateManager
            userStateManager.updateUserInfo(null, mAvatarUrl.toString());
            toast("头像更新成功");
            return;
        }

        // 正式版本：上传到服务器
        EasyHttp.post(this)
                .api(new UpdateImageApi()
                        .setImage(file))
                .request(new HttpCallback<HttpData<String>>(this) {

                    @Override
                    public void onSucceed(HttpData<String> data) {
                        mAvatarUrl = Uri.parse(data.getData());
                        GlideApp.with(getActivity())
                                .load(mAvatarUrl)
                                .transform(new MultiTransformation<>(new CenterCrop(), new CircleCrop()))
                                .into(mAvatarView);
                        
                        // 保存头像URL到UserStateManager
                        userStateManager.updateUserInfo(null, mAvatarUrl.toString());
                        toast("头像更新成功");
                        
                        if (deleteFile) {
                            file.delete();
                        }
                    }
                    
                    @Override
                    public void onFail(Exception e) {
                        super.onFail(e);
                        toast("头像上传失败，请重试");
                    }
                });
    }
}