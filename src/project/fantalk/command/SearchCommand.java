package project.fantalk.command;

import java.util.List;

import project.fantalk.api.Utils;
import project.fantalk.api.fanfou.FanfouService;
import project.fantalk.api.fanfou.Status;
import project.fantalk.model.Datastore;
import project.fantalk.model.Member;
import project.fantalk.xmpp.Message;
import project.fantalk.xmpp.XMPPUtils;

public class SearchCommand extends BaseCommand {

	public SearchCommand() {
		super("search");
	}

	@Override
	public void doCommand(Message message, String content) {
		String email = message.email;
		Member m = (Member) Datastore.getInstance().getAndCacheMember(email);
		if (!Utils.isEmpty(m.getUsername())) {
			FanfouService fanfou = new FanfouService(m);
			StringBuffer sb = new StringBuffer();
			if (!Utils.isEmpty(content)) {
				List<Status> statusList = fanfou.search(content);
				sb.append("搜索结果如下： ");
				sb.append(HotCommand.processStatuses(m, statusList).toString()
						.replaceAll("<b>", "").replaceAll("</b>", ""));
			} else {
				sb.append("搜索命令格式不正确，请添加搜索的关键字");
			}
			XMPPUtils.sendMessage(sb.toString(), email);
		} else {
			XMPPUtils.sendMessage("未绑定饭否帐号\n", email);
			return;
		}
	}

	public String documentation() {
		return "-search queryWord\n  功能说明：搜索fanfou消息，queryWorde为搜索的关键字";
	}
}
