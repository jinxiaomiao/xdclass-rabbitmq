package net.xdclass.xdclassrabbitmq;

import net.xdclass.xdclassrabbitmq.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class XdclassRabbitmqApplicationTests {

    @Autowired
    private RabbitTemplate template;

    @Test
    void send() {
        template.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,"order.new","新订单来啦1");
    }

    /**
     * 生产者到交换机的消息可靠性投递测试
     */
    @Test
    void testConfirmCallback() {

        template.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             *
             * @param correlationData 配置
             * @param ack 交换机是否收到消息，true是成功，false是失败
             * @param cause 失败的原因
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                System.out.println("confirm=====>");
                System.out.println("confirm===== correlationData="+correlationData);
                System.out.println("confirm==== ack="+ack);
                System.out.println("confirm==== cause="+cause);

                //根据ACK状态做对应的消息更新操作
                if(ack) {
                    System.out.println("发送成功");
                    //更新数据库，发送消息的状态为成功
                }else {
                    System.out.println("发送失败，记录到日志或数据库");
                    //更新数据库，发送消息的状态为失败
                }
            }
        });
        //数据库新增一个消息记录，状态是发帝 TODO

        //发送消息
        template.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,"order.new","新订单来啦1");
    }

    /**
     * 交换机到消费者的消息可靠性投递测试
     */
    @Test
    void testReturnCallback() {
        //为true,则交换机处理消息到路由失败，则会返回给生产者
        //开启强制消息投递（mandatory为设置为true），但消息未被路由至任何一个queue，则回退一条消息
        template.setReturnsCallback(new RabbitTemplate.ReturnsCallback() {
            @Override
            public void returnedMessage(ReturnedMessage returned) {
                int code = returned.getReplyCode();
                System.out.println("code="+code);
                System.out.println("returned="+returned.toString());
            }
        });
        template.convertAndSend(RabbitMQConfig.EXCHANGE_NAME,"order.new","ReturnsCallback新订单来啦11");
    }
}
