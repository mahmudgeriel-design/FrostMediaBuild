package my.frost.media;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.*;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {
    // База поданных заявок на пиар: Ник -> Список ссылок
    private final Map<String, List<String>> mediaRequests = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("media").setExecutor(this);
        getCommand("mediaadmin").setExecutor(this);
        
        // Мощный брендовый ASCII-арт FrostWorld при запуске в консоль
        Bukkit.getConsoleSender().sendMessage("§b");
        Bukkit.getConsoleSender().sendMessage("§b  ______                 _   __  __          _ _       ");
        Bukkit.getConsoleSender().sendMessage("§b |  ____|               | | |  \\/  |        | (_)      ");
        Bukkit.getConsoleSender().sendMessage("§b | |__ _ __ ___  ___  __| |_| \\  / | ___  __| |_  __ _ ");
        Bukkit.getConsoleSender().sendMessage("§b |  __| '__/ _ \\/ __|/ _` | | |\\/| |/ _ \\/ _` | |/ _` |");
        Bukkit.getConsoleSender().sendMessage("§b | |  | | | (_) \\__ \\ (_| | | |  | |  __/ (_| | | (_| |");
        Bukkit.getConsoleSender().sendMessage("§b |_|  |_|  \\___/|___/\\__,_|_|_|  |_|\\___|\\__,_|_|\\__,_|");
        Bukkit.getConsoleSender().sendMessage("§3» [FrostMedia] Авторская система пиара успешно запущена!");
        Bukkit.getConsoleSender().sendMessage("§3» © 2026 FrostWorld Project. Все права защищены LICENSE.txt\n");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Команда для обычных игроков /media <ссылка>
        if (cmd.getName().equalsIgnoreCase("media")) {
            if (!(sender instanceof Player player)) return true;

            if (args.length != 1) {
                player.sendMessage("§b§lFROSTWORLD §8» §cИспользуйте: /media <ссылка_на_видео_TikTok/Shorts>");
                return true;
            }

            String url = args[0].toLowerCase();
            if (!url.contains("tiktok.com") && !url.contains("youtube.com") && !url.contains("vk.com")) {
                player.sendMessage("§b§lFROSTWORLD §8» §cВы можете отправить ссылку только на TikTok, YouTube Shorts или ВК Клипы!");
                return true;
            }

            List<String> playerUrls = mediaRequests.computeIfAbsent(player.getName(), k -> new ArrayList<>());
            if (playerUrls.contains(url)) {
                player.sendMessage("§b§lFROSTWORLD §8» §cВы уже отправляли это видео на проверку!");
                return true;
            }

            playerUrls.add(url);
            player.sendMessage("§b§lFROSTWORLD §8» §aВаше видео успешно отправлено на проверку Создателю! Ожидайте начисления награды.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);

            // Оповещаем кураторов/админов в онлайне
            for (Player op : Bukkit.getOnlinePlayers()) {
                if (op.isOp() || op.hasPermission("frost.media.admin")) {
                    op.sendMessage("§d§l[FROST-ПИАР] §eИгрок §f" + player.getName() + " §eотправил видео на проверку! Проверить: /mediaadmin list");
                }
            }
            return true;
        }

        // Твоя админ-команда /mediaadmin
        if (cmd.getName().equalsIgnoreCase("mediaadmin")) {
            if (!sender.hasPermission("frost.media.admin") && !sender.isOp()) {
                sender.sendMessage("§cУ вас нет прав Администратора!");
                return true;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                sender.sendMessage("\n§b§l📝 СПИСОК ЗАЯВОК НА БОНУС ЗА ПИАР FROSTWORLD: §r\n");
                if (mediaRequests.isEmpty()) {
                    sender.sendMessage(" §7• §7Заявок пока нет.");
                    return true;
                }
                for (Map.Entry<String, List<String>> entry : mediaRequests.entrySet()) {
                    sender.sendMessage(" §7• §e" + entry.getKey() + " §7- Ссылок: §b" + entry.getValue().size());
                    for (String s : entry.getValue()) {
                        sender.sendMessage("   §8» §f" + s);
                    }
                }
                return true;
            }

            if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
                String targetName = args[1];
                if (!mediaRequests.containsKey(targetName)) {
                    sender.sendMessage("§cУ этого игрока нет активных заявок!");
                    return true;
                }

                mediaRequests.remove(targetName);
                
                // Выполняем консольную команду из нашего config.yml
                String cmdTemplate = getConfig().getString("settings.reward-command", "crate give %player% donate 1");
                String finalCmd = cmdTemplate.replace("%player%", targetName);
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCmd);

                sender.sendMessage("§b§lFROSTWORLD §8» §aВы успешно одобрили пиар-заявку игрока §e" + targetName + "§a! Награда выдана.");
                
                Player targetPlayer = Bukkit.getPlayer(targetName);
                if (targetPlayer != null) {
                    targetPlayer.sendMessage("§b§lFROSTWORLD §8» §a§lПОЗДРАВЛЯЕМ! §aВаше видео одобрено Создателем! Вам начислен Донат-Кейс.");
                    targetPlayer.playSound(targetPlayer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                }
                return true;
            }

            sender.sendMessage("§cИспользуйте: /mediaadmin list или /mediaadmin accept <ник>");
            return true;
        }
        return true;
    }
}