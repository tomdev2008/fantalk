package project.fantalk.command;

import java.util.Map;
import java.util.logging.Logger;

import project.fantalk.api.Utils;
import project.fantalk.api.fanfou.FanfouService;
import project.fantalk.model.Datastore;
import project.fantalk.model.Member;
import project.fantalk.xmpp.Message;
import project.fantalk.xmpp.XMPPUtils;

import com.google.appengine.api.xmpp.JID;


public class ReplyHandler extends BaseCommand {
    private static final Logger logger=Logger.getLogger(ReplyHandler.class.getName());

    public ReplyHandler() {
        super("reply","re","r","@","0","00");
    }

    @Override
    public void doCommand(Message message, String argument) {
        String email = message.email;
        JID sender = message.sender;
        if (Utils.isEmpty(argument)) {
            // 暂时只支持使用短ID
            XMPPUtils.sendMessage("格式错误，回复消息的格式为: -re id content.", sender);
            return;
        }
        String[] args = argument.split("\\s+", 2);
        if (args.length < 2) {
            XMPPUtils.sendMessage("格式错误，回复消息的格式为: -re id content.", sender);
            return;
        }
        Datastore ds = Datastore.getInstance();
        Map<Integer, String> map = ds.getStatusesCache(email);
        if (map!=null && !map.isEmpty()) {
            String reply = args[1];
            int id = Utils.toInt(args[0]);
            if (id >= 0 && id < map.size()) {
                String s = map.get(id);
                String[] ss = Utils.process(s);
                String statusId = ss[0];
                String name = ss[1];
//                String text = ss[2];//原消息内容
                Member m = (Member) ds.getAndCacheMember(email);
                String username = m.getUsername();
                if (!Utils.isEmpty(username)) {
                    FanfouService fs = new FanfouService(m);
                    if (!fs.reply("@" + name + " " + reply, statusId)) {
                        XMPPUtils.sendMessage("回复失败", sender);
                    }
                } else {
                    XMPPUtils.sendMessage("未绑定饭否帐号，回复失败", sender);
                }
            } else {
                XMPPUtils.sendMessage("无效ID，回复失败", sender);
            }
        } else {
            XMPPUtils.sendMessage("消息没有缓存，无法回复", sender);
        }
    }

    public String documentation() {
         return "-re id content\n回复某条消息，参数为消息ID(接受的消息后面带的那个ID)和回复内容";
    }

}
