package mao;

import com.rabbitmq.client.Channel;
import mao.tools.RabbitMQ;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

/**
 * Project name(项目名称)：rabbitMQ消息发布确认之单个确认发布
 * Package(包名): mao
 * Class(类名): Producer
 * Author(作者）: mao
 * Author QQ：1296193245
 * GitHub：https://github.com/maomao124/
 * Date(创建日期)： 2022/4/22
 * Time(创建时间)： 18:25
 * Version(版本): 1.0
 * Description(描述)：
 * 这是一种简单的确认方式，它是一种同步确认发布的方式，也就是发布一个消息之后只有它
 * 被确认发布，后续的消息才能继续发布,waitForConfirmsOrDie(long)这个方法只有在消息被确认
 * 的时候才返回，如果在指定时间范围内这个消息没有被确认那么它将抛出异常。
 * 这种确认方式有一个最大的缺点就是:发布速度特别的慢，因为如果没有确认发布的消息就会
 * 阻塞所有后续消息的发布，这种方式最多提供每秒不超过数百条发布消息的吞吐量。当然对于某
 * 些应用程序来说这可能已经足够了。
 *
 * 发送速度：5600条每秒
 * 1000条：324.664毫秒
 */

public class Producer
{
    private static final String QUEUE_NAME = "work";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException
    {
        Channel channel = RabbitMQ.getChannel();
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //在此信道上启用发布者确认
        channel.confirmSelect();

        //------------------------------------------------------
        long startTime = System.nanoTime();   //获取开始时间
        //------------------------------------------------------
        for (int i = 0; i < 1000;i++)
        {
            String message = "消息" + (i + 1);
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            //等到自上次调用以来发布的所有消息都已被代理确认或确认。
            // 请注意，当在非确认通道上调用时，waitForConfirms 会引发 IllegalStateException。
            boolean waitForConfirms = channel.waitForConfirms();
            if (waitForConfirms)
            {
                System.out.println("消息：\"" + message + "\"发送成功");
            }
            else
            {
                System.out.println("消息：\"" + message + "\"发送失败");
            }
        }
        //------------------------------------------------------
        long endTime = System.nanoTime(); //获取结束时间
        if ((endTime - startTime) < 1000000)
        {
            double final_runtime;
            final_runtime = (endTime - startTime);
            final_runtime = final_runtime / 1000;
            System.out.println("算法运行时间： " + final_runtime + "微秒");
        }
        else if ((endTime - startTime) >= 1000000 && (endTime - startTime) < 10000000000L)
        {
            double final_runtime;
            final_runtime = (endTime - startTime) / 1000;
            final_runtime = final_runtime / 1000;
            System.out.println("算法运行时间： " + final_runtime + "毫秒");
        }
        else
        {
            double final_runtime;
            final_runtime = (endTime - startTime) / 10000;
            final_runtime = final_runtime / 100000;
            System.out.println("算法运行时间： " + final_runtime + "秒");
        }
        Runtime r = Runtime.getRuntime();
        float memory;
        memory = r.totalMemory();
        memory = memory / 1024 / 1024;
        System.out.printf("JVM总内存：%.3fMB\n", memory);
        memory = r.freeMemory();
        memory = memory / 1024 / 1024;
        System.out.printf(" 空闲内存：%.3fMB\n", memory);
        memory = r.totalMemory() - r.freeMemory();
        memory = memory / 1024 / 1024;
        System.out.printf("已使用的内存：%.4fMB\n", memory);
        //------------------------------------------------------

        System.out.println("消息全部发送完成");
    }
}
