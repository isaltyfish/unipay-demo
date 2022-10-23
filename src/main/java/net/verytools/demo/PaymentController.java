package net.verytools.demo;

import net.verytools.unipay.api.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class PaymentController {

    @Resource
    PaymentService paymentService;

    /**
     * 统一下单，生成二维码内容。
     * resources中zfb.properties/wx.properties是商户配置信息，配置正确后，再调试。
     *
     * @return 生成二维码所需字符串。
     */
    @GetMapping("/placeOrder")
    public String unifyOrder() {
        return paymentService.unifyOrder(PayType.alipay);
    }

    /**
     * 支付回调。
     */
    @RequestMapping("/callback/{payType}")
    @ResponseBody
    public String handlePayNotify(@PathVariable String payType, HttpServletRequest request) {
        PayNotifyHandler h = NotifyHandlerFactory.getNotifyHandler(payType); // payType 是 wx或者alipay
        return h.handle(request, new PayNotifyCallback() {

            @Override
            public void onPaySuccess(String outTradeNo, Map<String, String> notifyParas) {
                // 用户支付成功后，在这里处理支付成功后的业务逻辑。
            }

            @Override
            public boolean isNotifyHandled(String outTradeNo) {
                // 根据 outTradeNo 查询数据库中该订单是否已经支付成功，如果支付成功了返回 true，否则返回 false。
                // 返回 false 则会执行 onPaySuccess，在 onPaySuccess 中处理支付成功后的逻辑。
                return false;
            }

            @Override
            public MchInfo resolveMchInfo(Map<String, String> notifyParas) {
                // 支付回调需要校验支付通知的真伪，需要商户信息，这里返回校验需要的商户信息。
                return payType.equals(PayType.alipay.toString()) ? MchInfo.create(PayType.alipay, "zfb.properties") : MchInfo.create(PayType.wx, "wx.properties");
            }
        }, null);
    }

}
