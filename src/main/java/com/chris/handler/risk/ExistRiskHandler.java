package com.chris.handler.risk;

import com.chris.bean.command.CmdResultCode;
import com.chris.bean.command.RbCmd;
import com.chris.common.order.CmdType;
import com.chris.handler.BaseHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.collections.api.set.primitive.MutableIntSet;
import org.eclipse.collections.api.set.primitive.MutableLongSet;

@RequiredArgsConstructor
@Log4j2
public class ExistRiskHandler extends BaseHandler {

    @NonNull
    private MutableLongSet uidSet;

    @NonNull
    private MutableIntSet codeSet;

    // 发布行情， 新委托event，撤单event
    @Override
    public void onEvent(RbCmd cmd, long sequence, boolean endOfBatch) throws Exception {

        //行情发布指令，不用判断
        if(cmd.command == CmdType.HQ_PUB){
            return;
        }
        //1. user exist or not
        //2. stock valid or not
        if(cmd.command == CmdType.NEW_ORDER || cmd.command == CmdType.CANCEL_ORDER){
            if(!uidSet.contains(cmd.uid)){
                log.warn("illegal uid[{}] exists", cmd.uid);
                cmd.resultCode = CmdResultCode.RISK_INVALID_USER;
                return;
            }
            if(!codeSet.contains(cmd.code)){
                log.warn("illegal stock code[{}] exists", cmd.code);
                cmd.resultCode = CmdResultCode.RISK_INVALID_CODE;
                return;
            }
        }

    }
}
