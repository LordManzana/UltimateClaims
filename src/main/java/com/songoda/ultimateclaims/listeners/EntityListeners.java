package com.songoda.ultimateclaims.listeners;

import com.songoda.ultimateclaims.UltimateClaims;
import com.songoda.ultimateclaims.claim.Claim;
import com.songoda.ultimateclaims.claim.ClaimManager;
import com.songoda.ultimateclaims.member.ClaimMember;
import com.songoda.ultimateclaims.member.ClaimRole;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class EntityListeners implements Listener {

    UltimateClaims plugin;

    public EntityListeners(UltimateClaims plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getChunk() == event.getTo().getChunk()) return;

        ClaimManager claimManager = plugin.getClaimManager();

        if (claimManager.hasClaim(event.getFrom().getChunk())) {
            Claim claim = claimManager.getClaim(event.getFrom().getChunk());
            if (claimManager.getClaim(event.getTo().getChunk()) != claim) {
                ClaimMember member = claim.getMember(event.getPlayer());
                if (member != null) {
                    if (member.getRole() == ClaimRole.VISITOR)
                        claim.removeMember(member);
                    else
                        member.setPresent(false);
                }
                plugin.getLocale().getMessage("event.claim.exit")
                        .processPlaceholder("claim", claim.getName())
                        .sendPrefixedMessage(event.getPlayer());
            }
        }

        if (claimManager.hasClaim(event.getTo().getChunk())) {
            Claim claim = claimManager.getClaim(event.getTo().getChunk());
            if (claimManager.getClaim(event.getFrom().getChunk()) != claim) {
                ClaimMember member = claim.getMember(event.getPlayer());
                if (member == null) {
                    if (claim.isLocked() && !event.getPlayer().hasPermission("ultimateclaims.bypass")) {
                        plugin.getLocale().getMessage("event.claim.locked")
                                .sendPrefixedMessage(event.getPlayer());
                        event.setCancelled(true);
                        return;
                    }

                    if (event.getPlayer().hasPermission("ultimateclaims.bypass")) {
                        claim.addMember(event.getPlayer(), ClaimRole.VISITOR);
                        member = claim.getMember(event.getPlayer());
                    }
                }
                if (member != null)
                    member.setPresent(true);
                plugin.getLocale().getMessage("event.claim.enter")
                        .processPlaceholder("claim", claim.getName())
                        .sendPrefixedMessage(event.getPlayer());
            }
        }
    }
}
