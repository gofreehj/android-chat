package cn.wildfire.chat.kit.voip.conference;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Date;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import cn.wildfire.chat.kit.AppServiceProvider;
import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.R2;
import cn.wildfire.chat.kit.WfcBaseActivity;
import cn.wildfire.chat.kit.WfcUIKit;
import cn.wildfire.chat.kit.voip.conference.model.ConferenceInfo;
import cn.wildfirechat.avenginekit.AVEngineKit;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;

public class ConferenceInfoActivity extends WfcBaseActivity {
    private String conferenceId;
    private String password;
    private ConferenceInfo conferenceInfo;
    @BindView(R2.id.titleTextView)
    TextView titleTextView;
    @BindView(R2.id.ownerTextView)
    TextView ownerTextView;
    @BindView(R2.id.callIdTextView)
    TextView callIdTextView;
    @BindView(R2.id.startDateTimeTextView)
    TextView startDateTimeView;
    @BindView(R2.id.endDateTimeTextView)
    TextView endDateTimeView;
    @BindView(R2.id.audioSwitch)
    SwitchMaterial audioSwitch;
    @BindView(R2.id.videoSwitch)
    SwitchMaterial videoSwitch;
    @BindView(R2.id.joinConferenceBtn)
    Button joinConferenceButton;

    private MenuItem destroyItem;
    private MenuItem favItem;
    private MenuItem unFavItem;

    @Override
    protected int contentLayout() {
        return R.layout.av_conference_info_activity;
    }

    @Override
    protected int menu() {
        return R.menu.conference_info;
    }

    @Override
    protected void afterMenus(Menu menu) {
        super.afterMenus(menu);
        destroyItem = menu.findItem(R.id.destroy);
        favItem = menu.findItem(R.id.fav);
        unFavItem = menu.findItem(R.id.unfav);
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        Intent intent = getIntent();
        conferenceId = intent.getStringExtra("conferenceId");
        password = intent.getStringExtra("password");
        WfcUIKit.getWfcUIKit().getAppServiceProvider().queryConferenceInfo(conferenceId, password, new AppServiceProvider.QueryConferenceInfoCallback() {
            @Override
            public void onSuccess(ConferenceInfo info) {
                setupConferenceInfo(info);
            }

            @Override
            public void onFail(int code, String msg) {
                Toast.makeText(ConferenceInfoActivity.this, "获取会议详情失败", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.destroy) {
            WfcUIKit.getWfcUIKit().getAppServiceProvider().destroyConference(conferenceId, new GeneralCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(ConferenceInfoActivity.this, "销毁会议成功", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFail(int i) {
                    Toast.makeText(ConferenceInfoActivity.this, "销毁会议失败 " + i, Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R2.id.conferenceQRCodeLinearLayout)
    void showConferenceQRCode() {
        // TODO
    }

    @OnClick(R2.id.joinConferenceBtn)
    void joinConference() {
        ConferenceInfo info = conferenceInfo;
        boolean audience = info.isAudience() || (!audioSwitch.isChecked() && !videoSwitch.isChecked());
        AVEngineKit.CallSession session = AVEngineKit.Instance().joinConference(info.getConferenceId(), false, info.getPin(), info.getOwner(), info.getConferenceTitle(), "", audience, info.isAdvance(), !audioSwitch.isChecked(), !videoSwitch.isChecked(), null);
        if (session != null) {
            Intent intent = new Intent(this, ConferenceActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "加入会议失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupConferenceInfo(ConferenceInfo info) {
        conferenceInfo = info;
        titleTextView.setText(info.getConferenceTitle());
        String owner = info.getOwner();
        String ownerName = ChatManager.Instance().getUserDisplayName(owner);
        ownerTextView.setText(ownerName);
        callIdTextView.setText(info.getConferenceId());
        startDateTimeView.setText(new Date(info.getStartTime() * 1000).toString());
        endDateTimeView.setText(new Date(info.getEndTime() * 1000).toString());

        long now = System.currentTimeMillis() / 1000;
        if (now > info.getEndTime()) {
            joinConferenceButton.setEnabled(false);
            joinConferenceButton.setText("会议已结束");
        } else if (now < info.getStartTime()) {
            joinConferenceButton.setEnabled(false);
            joinConferenceButton.setText("会议未开始");
        } else {
            joinConferenceButton.setEnabled(true);
            joinConferenceButton.setText("加入会议");
        }

        if (Objects.equals(owner, ChatManager.Instance().getUserId())) {
            destroyItem.setVisible(true);
        } else {
            // TODO
        }
    }
}
