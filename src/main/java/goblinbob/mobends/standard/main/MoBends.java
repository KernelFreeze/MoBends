package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.error.ErrorReportRegistry;
import goblinbob.mobends.core.exceptions.InvalidPackFormatException;
import goblinbob.mobends.core.kumo.driver.DriverFunctionRegistry;
import goblinbob.mobends.core.kumo.driver.DriverLayerTemplate;
import goblinbob.mobends.core.kumo.driver.node.LookAroundDriverNodeTemplate;
import goblinbob.mobends.core.kumo.driver.node.StandardDriverNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.MovementKeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.StandardKeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.*;
import goblinbob.mobends.forge.DataUpdateHandler;
import goblinbob.mobends.forge.EntityData;
import goblinbob.mobends.forge.ReportOutput;
import goblinbob.mobends.forge.SerialContext;
import goblinbob.mobends.forge.client.event.KeyboardHandler;
import goblinbob.mobends.forge.client.event.RenderHandler;
import goblinbob.mobends.forge.trigger.EquipmentNameCondition;
import goblinbob.mobends.standard.main.trigger.WolfStateCondition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.stream.Collectors;

@Mod(ModStatics.MODID)
public class MoBends
{
    public static final Logger LOGGER = LogManager.getLogger();

    private final SerialContext serialContext;
    private final TestBenderProvider benderProvider;
    private final DriverFunctionRegistry<EntityData> driverFunctionRegistry;
    private final KeyboardHandler keyboardHandler;

    private boolean wolfAnimated = true;

    public MoBends()
    {
        this.serialContext = new SerialContext();
        this.benderProvider = new TestBenderProvider(this.serialContext);
        this.keyboardHandler = new KeyboardHandler(this::onRefresh);

        this.serialContext.layerRegistry.register("core:keyframe", KeyframeLayerTemplate::deserialize);
        this.serialContext.layerRegistry.register("core:driver", DriverLayerTemplate::deserialize);

        this.serialContext.keyframeNodeRegistry.register("core:standard", StandardKeyframeNodeTemplate::deserialize);
        this.serialContext.keyframeNodeRegistry.register("core:movement", MovementKeyframeNodeTemplate::deserialize);

        this.serialContext.driverNodeRegistry.register("core:standard", StandardDriverNodeTemplate::deserialize);
        this.serialContext.driverNodeRegistry.register("core:look_around", LookAroundDriverNodeTemplate::deserialize);

        this.serialContext.triggerConditionRegistry.register("core:or", OrCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:and", AndCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:not", NotCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:state", StateCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:ticks_passed", TicksPassedCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:equipment_name", EquipmentNameCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:animation_finished", AnimationFinishedCondition.Template::deserialize);

        this.serialContext.triggerConditionRegistry.register("mobends:wolf_state", WolfStateCondition.Template::deserialize);

        this.driverFunctionRegistry = new DriverFunctionRegistry<>();
        StandardDriverFunctions.popualteRegistry(this.driverFunctionRegistry);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this.keyboardHandler);
        MinecraftForge.EVENT_BUS.register(new RenderHandler(benderProvider, driverFunctionRegistry));
        MinecraftForge.EVENT_BUS.register(new DataUpdateHandler(benderProvider::updateDataOnClientTick));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);
    }

    public void onRefresh()
    {
        this.benderProvider.refresh();

        // Toggling the wolf bender ON/OFF
        this.wolfAnimated = !this.wolfAnimated;
        this.benderProvider.wolfBender.setAnimate(wolfAnimated);

        Core core = new Core();
        ReportOutput reportOutput = new ReportOutput();
        ErrorReportRegistry reportRegistry = new ErrorReportRegistry(reportOutput);

        core.registerErrors(reportRegistry);
        reportRegistry.report(new InvalidPackFormatException("Bruh", "Waddup"));
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("PRE-INIT PHASE");
    }

    @SubscribeEvent
    public void setupClient(final FMLClientSetupEvent event)
    {
        this.keyboardHandler.setup();

        try
        {
            this.benderProvider.init();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo(ModStatics.MODID, "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    @SubscribeEvent
    public void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event)
    {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }
}
