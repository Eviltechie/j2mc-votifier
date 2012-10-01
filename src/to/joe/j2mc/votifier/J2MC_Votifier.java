package to.joe.j2mc.votifier;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import to.joe.j2mc.core.J2MC_Manager;
import to.joe.j2mc.redemption.J2MC_Redemption;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class J2MC_Votifier extends JavaPlugin implements Listener {
    
    boolean logVotes;
    boolean hats;
    
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        logVotes = getConfig().getBoolean("logvotes");
        hats = getConfig().getBoolean("hats");
        
        J2MC_Manager.getPermissions().addFlagPermissionRelation("j2mc.admintoolkit.hat", 'H', true);
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onVote(VotifierEvent event) {
        if (!logVotes)
            return;
        Vote v = event.getVote();
        Logger l = getLogger();
        
        try {
            PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO votes (address, service, timestamp, username) VALUES (?,?,?,?)");
            ps.setString(1, v.getAddress());
            ps.setString(2, v.getServiceName());
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
            ps.setTimestamp(3, new Timestamp(sdf.parse(v.getTimeStamp()).getTime()));
            ps.setString(4, v.getUsername());
            ps.execute();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Something went wrong logging the vote in the database", e);
        } catch (ParseException e) {
            getLogger().log(Level.SEVERE, "Date parse error. Votifier must have fed us garbage", e);
        }
        
        getServer().broadcastMessage(ChatColor.RED + v.getUsername() + ChatColor.AQUA + " has just voted for the server!");
        getServer().broadcastMessage(ChatColor.RED + "Visit http://joe.to/vote for details on how to vote and claim rewards");
        
        try {
            int id = J2MC_Redemption.newCoupon(v.getUsername(), false, v.getServiceName(), System.currentTimeMillis() / 1000L + 86400, 1);
            if (id != -1) {
                int[] prizes = {2256,2257,2258,2259,2260,2261,2262,2263,2264,2265,2266,84};
                Collections.shuffle(Arrays.asList(prizes));
                J2MC_Redemption.addItem(id, prizes[0]);
            }
        } catch (SQLException e) {
            l.log(Level.SEVERE, "Error adding voting rewards", e);
        }
        
        l.info(v.getAddress());
        l.info(v.getServiceName());
        l.info(v.getTimeStamp());
        l.info(v.getUsername());
    }
    
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!hats)
            return;
        try {
            PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("SELECT * FROM votes WHERE username LIKE ? AND timestamp > DATE_SUB( NOW(), INTERVAL 24 HOUR)");
            ps.setString(1, event.getPlayer().getName());
            ResultSet rs = ps.executeQuery();
            Player p = event.getPlayer();
            if (rs.next()) {
                p.sendMessage(ChatColor.GREEN + "Thanks for voting in the past 24 hours");
                p.sendMessage(ChatColor.GREEN + "As a reward, you have access to /hat");
                J2MC_Manager.getPermissions().addFlag(p, 'H');
            } else {
                p.sendMessage(ChatColor.RED + "You have not voted in the past 24 hours.");
                ItemStack headgear = p.getInventory().getHelmet();
                if (headgear == null || headgear.getType().equals(Material.GOLD_HELMET) || headgear.getType().equals(Material.IRON_HELMET) || headgear.getType().equals(Material.DIAMOND_HELMET) || headgear.getType().equals(Material.LEATHER_HELMET) || headgear.getType().equals(Material.CHAINMAIL_HELMET)) {
                    p.sendMessage(ChatColor.RED + "Visit http://joe.to/vote for details on voting");
                } else {
                    p.sendMessage(ChatColor.RED + "Your hat has been removed");
                    p.getInventory().setHelmet(null);
                }
            }
        } catch (SQLException e) {
            getServer().getLogger().log(Level.SEVERE, "Error reading if player can has hat", e);
        }
    }
}
