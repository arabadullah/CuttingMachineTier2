package gtPlusPlus.core.handler.events;

import java.util.*;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import gtPlusPlus.api.analytics.SegmentAnalytics;
import gtPlusPlus.api.analytics.SegmentHelper;
import gtPlusPlus.core.lib.CORE;
import gtPlusPlus.core.lib.LoadedMods;
import gtPlusPlus.core.proxy.ClientProxy;
import gtPlusPlus.core.util.Utils;
import gtPlusPlus.core.util.player.PlayerCache;
import gtPlusPlus.core.util.player.PlayerUtils;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class LoginEventHandler {

	public String localPlayersName;
	public UUID localPlayersUUID;
	private EntityPlayer localPlayerRef;

	@SubscribeEvent
	public void onPlayerLogin(final PlayerEvent.PlayerLoggedInEvent event) {

		this.localPlayerRef = event.player;
		this.localPlayersName = event.player.getDisplayName();
		this.localPlayersUUID = event.player.getUniqueID();

		//Set this for easier use elsewhere.
		if (event.player.getEntityWorld().isRemote){
			ClientProxy.playerName = this.localPlayersName;			
		}	

		try {			
			//new SegmentAnalytics(event.player);	
			
			
			Map<String, Object> properties = new LinkedHashMap<>();
			properties.put("username", this.localPlayersName);
			properties.put("gt_version", Utils.getGregtechVersionAsString());
			if (LoadedMods.IndustrialCraft2){
				properties.put("ic2_version", IC2.VERSION);
			}
			properties.put("country_code", CORE.USER_COUNTRY);
			properties.put("gtnh", CORE.GTNH);			
			SegmentHelper.getInstance().addUser(this.localPlayersUUID.toString(), properties);
			
		}
		catch (Throwable t){
			SegmentAnalytics.LOG("Failed to create Analytics submission during log in process.");
		}

		try {


			if (this.localPlayerRef instanceof EntityPlayerMP){

				//Populates player cache
				if (!this.localPlayerRef.worldObj.isRemote){
					PlayerCache.appendParamChanges(this.localPlayersName, this.localPlayersUUID.toString());

					if (CORE.ConfigSwitches.enableUpdateChecker){
						if (!Utils.isModUpToDate()){
							Utils.LOG_INFO("[GT++] You're not using the latest recommended version of GT++, consider updating.");
							if (!CORE.MASTER_VERSION.toLowerCase().equals("offline")) {
								Utils.LOG_INFO("Latest version is: "+CORE.MASTER_VERSION);
							}
							Utils.LOG_INFO("You currently have: "+CORE.VERSION);
							ShortTimer(this.localPlayerRef, 20);						
						}
						else {
							Utils.LOG_INFO("You're using the latest recommended version of GT++.");
						}
					}

				}


				/*if (localPlayerRef.getCommandSenderName().toLowerCase().equalsIgnoreCase("ImQ009") || localPlayerRef.getCommandSenderName().toLowerCase().contains("player")){
					Utils.LOG_INFO("Spawning a new Santa Thread.");
					Thread t = new Thread() {
						UUID threadHandlerIDthing = localPlayersUUID;
						@Override
						public void run() {
							while(true && Minecraft.getMinecraft().getIntegratedServer() != null) {
								try {
									if(localPlayerRef == null){
										localPlayerRef = Utils.getPlayerOnServerFromUUID(threadHandlerIDthing);
									}


									//ImQ009 is a legend.
									if (localPlayerRef.getCommandSenderName().toLowerCase().equalsIgnoreCase("ImQ009")){
										Utils.messagePlayer(localPlayerRef, "Enjoy some complimentary Raisin Bread.");
										localPlayerRef.inventory.addItemStackToInventory(UtilsItems.getSimpleStack(ModItems.itemIngotRaisinBread, MathUtils.randInt(1, 5)));
									}


									if (localPlayerRef.getCommandSenderName().toLowerCase().contains("player")){
										Utils.messagePlayer(localPlayerRef, "Enjoy some complimentary Raisin Bread.");
										localPlayerRef.inventory.addItemStackToInventory(UtilsItems.getSimpleStack(ModItems.itemIngotRaisinBread, MathUtils.randInt(1, 5)));
									}
									Thread.sleep(1000*60*MathUtils.randInt(15, 90));
								} catch (InterruptedException ie) {
									Utils.LOG_INFO("Santa Mode Disabled.");
								}
							}

							Utils.LOG_INFO("Thread Stopped. Handler Closed.");

						}
					};
					//t.start();


				}*/


			}
		} catch (final Throwable errr){
			Utils.LOG_INFO("Login Handler encountered an error.");

		}
	}

	//Handles notifying the player about a version update.
	public Timer ShortTimer(EntityPlayer localPlayer, final int seconds) {
		Timer timer;
		timer = new Timer();
		timer.schedule(new NotifyPlayer(localPlayer), seconds * 1000);
		return timer;
	}

	//Timer Task for notifying the player.
	class NotifyPlayer extends TimerTask {
		final EntityPlayer toMessage;
		public NotifyPlayer(EntityPlayer localPlayer) {
			toMessage = localPlayer;
		}

		@Override
		public void run() {
			if (toMessage != null){
				if (toMessage instanceof EntityPlayerMP){
					PlayerUtils.messagePlayer(toMessage, "You're not using the latest recommended version of GT++, consider updating.");					
				}
			}
		}
	}

}