package net.verytools.demo;

import net.verytools.unipay.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    /**
     * 统一下单接口，调用示例：
     *
     * @param payType PayType.wx/PayType.alipay
     */
    public String unifyOrder(PayType payType) {
        UnipayService service = UniPayServiceFactory.getUnipayService(payType); // 微信支付使用PayType.wx

        Order order = new Order();
        order.setSubject("腾讯充值中心-企鹅币充值"); // 商品
        // 交易编号，由我们自己设置，需保证唯一即可。
        order.setOutTradeNo("Q12345678923");
        // 支付金额，注意：无论支付宝还是微信，单位为**分**，内部会自动转换。
        order.setTotalFee(100);

        OrderContext context = new OrderContext();
        context.setNotifyUrl("http://www.youdomain/pay/notify/callback/" + payType); // 接收支付回调的url

        // 这里创建 MchInfo 使用了 MchInfo.create 方法，只是为了方便，完全可以将这些配置信息存储在任何地方，
        // 然后根据支付类型，实例化 AlipayMchInfo 或者 WxpayMchInfo 即可。
        MchInfo mchInfo = payType == PayType.alipay ? MchInfo.create(PayType.alipay, "zfb.properties") : MchInfo.create(PayType.wx, "wx.properties");
        PushOrderResult result = service.unifyOrder(context, order, mchInfo);
        if (result.isOk()) {
            // 将这个字符串传到网页上，使用一个二维码生成js库生成二维码就可以了。
            String qrcodeContent = result.getQrCodeContent();
            logger.info("qrcode content is: {}", qrcodeContent);
            return qrcodeContent;
        } else {
            logger.error("unify order error, msg: {}, code: {}", result.getMsg(), result.getCode());
            throw new RuntimeException("unify order failed");
        }
    }

}
