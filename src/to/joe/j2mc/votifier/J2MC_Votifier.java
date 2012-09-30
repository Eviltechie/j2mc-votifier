package to.joe.j2mc.votifier;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import to.joe.j2mc.core.J2MC_Manager;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class J2MC_Votifier extends JavaPlugin implements Listener {
    
    @Override
    public void onEnable() {
        /*try {
            PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("CREATE TABLE IF NOT EXISTS `votes` (  `id` int(11) NOT NULL AUTO_INCREMENT,  `playerID` int(11) NOT NULL,  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  PRIMARY KEY (`id`)) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;");
            ps.execute();
        } catch (SQLException e) {
            this.getLogger().log(Level.SEVERE, "Error creating votes table. Disabling plugin.", e);
            //TODO Kill the plugin here
        }*/
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    
    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote v = event.getVote();
        Logger l = getLogger();
        
        try {
            PreparedStatement ps = J2MC_Manager.getMySQL().getFreshPreparedStatementHotFromTheOven("INSERT INTO votes (address, service, timestamp, username) VALUES (?,?,?,?)");
            ps.setString(1, v.getAddress());
            ps.setString(2, v.getServiceName());
            ps.setString(3, v.getTimeStamp());
            ps.setString(4, v.getUsername());
            ps.execute();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Something went wrong logging the vote in the database", e);
        }
        
        l.info(v.getAddress());
        l.info(v.getServiceName());
        l.info(v.getTimeStamp());
        l.info(v.getUsername());
    }
}
