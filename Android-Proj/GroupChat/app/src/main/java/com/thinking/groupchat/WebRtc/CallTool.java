package com.thinking.groupchat.WebRtc;

/**
 * Created by Yu Yong on 2017/5/5.
 */

public class CallTool extends ConnTool {
    public CallTool(onResutListener listener) {
        super(listener);
    }

    @Override
    public void start(String _method, Object... _params) {
        mClass = this.getClass();
        super.start(_method, _params);
    }

    public void createOffer(Object... params) {
        if (mConns.size() == 2) {
            //假设两路视频，三方聊天
            mConns.get(0).createOffer();
        }
    }
}
