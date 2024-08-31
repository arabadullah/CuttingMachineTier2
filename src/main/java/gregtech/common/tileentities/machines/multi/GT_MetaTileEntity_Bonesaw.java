package gregtech.common.tileentities.machines.multi;

import com.gtnewhorizon.structurelib.alignment.constructable.ISurvivalConstructable;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.ISurvivalBuildEnvironment;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.ofBlock;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.onElementPass;
import static com.gtnewhorizon.structurelib.structure.StructureUtility.transpose;

import static gregtech.api.enums.GT_HatchElement.Energy;
import static gregtech.api.enums.GT_HatchElement.InputBus;
import static gregtech.api.enums.GT_HatchElement.InputHatch;
import static gregtech.api.enums.GT_HatchElement.Maintenance;
import static gregtech.api.enums.GT_HatchElement.Muffler;
import static gregtech.api.enums.GT_HatchElement.OutputBus;

import gregtech.api.enums.Materials;
import gregtech.api.enums.MaterialsUEVplus;
import gregtech.api.enums.TAE;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.IIconContainer;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.logic.ProcessingLogic;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Input;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.recipe.RecipeMaps;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import static gregtech.api.util.GT_StructureUtility.buildHatchAdder;
import gregtech.api.util.GT_Utility;
import gtPlusPlus.core.block.ModBlocks;
import gtPlusPlus.core.lib.CORE;
import gtPlusPlus.core.util.minecraft.PlayerUtils;
import gtPlusPlus.xmod.gregtech.api.metatileentity.implementations.base.GregtechMeta_MultiBlockBase;
import gtPlusPlus.xmod.gregtech.common.blocks.textures.TexturesGtBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import java.lang.Math;

public class GT_MetaTileEntity_Bonesaw extends
    GregtechMeta_MultiBlockBase<GT_MetaTileEntity_Bonesaw> implements ISurvivalConstructable {

    private int mCasing;
    private boolean MACHINE_MODE_PLASMA = false;
    private boolean MACHINE_MODE_SPATIAL = false;
    private int currentParallels;
    private float currentSpeedBonusDenominator = 350.0f;
    private int currentPlasmaConsumption = 0;

    private static IStructureDefinition<GT_MetaTileEntity_Bonesaw> STRUCTURE_DEFINITION = null;

    public GT_MetaTileEntity_Bonesaw(final int aID, final String aName,
                                     final String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_Bonesaw(final String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(final IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_Bonesaw(this.mName);
    }

    @Override
    public String getMachineType() {
        return "Cutting Machine";
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType(getMachineType())
            .addInfo("Controller Block for the Bonesaw")
            .addInfo("250% faster than using single block machines of the same voltage.")
            .addInfo("Uses 66.6% of the EU/t normally required.")
            .addInfo("Processes five* items per voltage tier.")
            .addInfo("*Max parallels can vary greatly. It Hungers.")
            .addPollutionAmount(getPollutionPerSecond(null))
            .addSeparator()
            .beginStructureBlock(3, 3, 5, true)
            .addController("Front Center")
            .addCasingInfoMin("Cutting Factory Frames", 14, false)
            .addInputBus("Any Casing", 1)
            .addOutputBus("Any Casing", 1)
            .addInputHatch("Any Casing", 1)
            .addEnergyHatch("Any Casing", 1)
            .addMaintenanceHatch("Any Casing", 1)
            .addMufflerHatch("Any Casing", 1)
            .toolTipFinisher(CORE.GT_Tooltip_Builder.get());
        return tt;
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_Bonesaw> getStructureDefinition() {
        if (STRUCTURE_DEFINITION == null) {
            STRUCTURE_DEFINITION = StructureDefinition.<GT_MetaTileEntity_Bonesaw>builder()
                .addShape(
                    mName,
                    transpose(
                        new String[][] { { "CCC", "CCC", "CCC", "CCC", "CCC" }, { "C~C", "C-C", "C-C", "C-C", "CCC" },
                            { "CCC", "CCC", "CCC", "CCC", "CCC" }, }))
                .addElement(
                    'C',
                    buildHatchAdder(GT_MetaTileEntity_Bonesaw.class)
                        .atLeast(InputBus, InputHatch, OutputBus, Maintenance, Energy, Muffler)
                        .casingIndex(getCasingTextureIndex())
                        .dot(1)
                        .buildAndChain(onElementPass(x -> ++x.mCasing, ofBlock(ModBlocks.blockCasings2Misc, 13))))
                .build();
        }
        return STRUCTURE_DEFINITION;
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(mName, stackSize, hintsOnly, 1, 1, 0);
    }

    @Override
    public int survivalConstruct(ItemStack stackSize, int elementBudget, ISurvivalBuildEnvironment env) {
        if (mMachine) return -1;
        return survivialBuildPiece(mName, stackSize, 1, 1, 0, elementBudget, env, false, true);
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        mCasing = 0;
        return checkPiece(mName, 1, 1, 0) && mCasing >= 14 && checkHatch();
    }

    @Override
    protected IIconContainer getActiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Default_Active;
    }

    @Override
    protected IIconContainer getInactiveOverlay() {
        return TexturesGtBlock.Overlay_Machine_Controller_Default;
    }

    @Override
    protected int getCasingTextureId() {
        return TAE.GTPP_INDEX(29);
    }

    @Override
    public RecipeMap<?> getRecipeMap() {
        return RecipeMaps.cutterRecipes;
    }

    @Override
    public int getRecipeCatalystPriority() {
        return -1;
    }

    @Override
    protected ProcessingLogic createProcessingLogic() {
        return new ProcessingLogic().setSpeedBonus(100.0f / this.currentSpeedBonusDenominator)
            .setEuModifier(0.666F)
            .setMaxParallelSupplier(this::getCurrentParallels);
    }

    public int getBaseParallelRecipes() {
        return (6 * GT_Utility.getTier(this.getMaxInputVoltage()));
    }

    public int getCurrentParallels() {
        return this.currentParallels;
    }

    @Override
    public int getMaxParallelRecipes() {
        return this.currentParallels;
    }

    public double spatiallyEnlargedConsumptionFormula() {
        // parameters for the efficiency curve
        // insert desmos link here
        // Could have rescaled the x-axis to be from 0-1
        // But I'm lazy so it stays as 0-8
        int x_axis_scaling_factor = 8;
        final float a = 2.65f;
        final float b1 = 0.8f;
        final float b2 = 0.2f;
        final float c = 4.0f;
        final float d = 0.8f;

        int stored_spatial = this.getFluidAndDrain(MaterialsUEVplus.Space.getFluid(0L));
        int hatch_capacity = this.getMaxCapacityOfAFluid(MaterialsUEVplus.Space.getFluid(0L));
        float percent_filled_factor = x_axis_scaling_factor * (float) stored_spatial / hatch_capacity;


        float fill_parallel_boost;
        if (percent_filled_factor <= 4) {
            fill_parallel_boost = logisticFunction(a,b1,c,d,percent_filled_factor) ;
        }
        else {
            fill_parallel_boost = logisticFunction(a,b2,c,d,percent_filled_factor);
        }
        // Multiplier for what tier Input Hatch(es) used for spatially enlarged fluid
        // I'm told spatially enlarged becomes free with T7 eoh so lets just balance it around LuV input hatches for now
        double hatch_tier_parallel_boost = Math.pow( Math.log(hatch_capacity / 512_000.0d+1) / Math.log(2.0d), 1.0f / 4 );

        return fill_parallel_boost * hatch_tier_parallel_boost;
    }

    private int getFluidAndDrain(FluidStack fluid) {
        int amount = 0;
        for (GT_MetaTileEntity_Hatch_Input fluid_hatch : this.mInputHatches) {
            if (fluid_hatch.getFluid().getFluid() == fluid.getFluid() && fluid_hatch.drain(fluid_hatch.getFluidAmount(), false).amount == fluid_hatch.mFluid.amount) {
                amount += fluid_hatch.getFluidAmount();
                this.drainInputHatch(fluid_hatch);
            }
        }
        return amount;
    }

    private int getFluidAmount(FluidStack fluid) {
        int amount = 0;
        for (GT_MetaTileEntity_Hatch_Input fluid_hatch : this.mInputHatches) {
            if (fluid_hatch.getFluid().getFluid() == fluid.getFluid()) {
                amount += fluid_hatch.getCapacity();
            }
        }
        return amount;
    }

    private void drainInputHatch(GT_MetaTileEntity_Hatch_Input input_hatch) {
        input_hatch.drain(input_hatch.getFluidAmount(), true);
    }

    private int getMaxCapacityOfAFluid(FluidStack fluid) {
        int hatch_capacity = 0;
        for (GT_MetaTileEntity_Hatch_Input fluid_hatch : this.mInputHatches) {
            if (fluid_hatch.getFluid().getFluid() == MaterialsUEVplus.Space.mFluid) {
                int amount = fluid_hatch.getCapacity();
                hatch_capacity += amount;
            }
        }
        return hatch_capacity;
    }

    private void doPlasmaConsumption() {
        int output;
        // subtracting by 350 to make minimum speed bonus not require plasma
        // takes the speed bonus that stacks on the base in liters of plasma every half a second
        this.currentPlasmaConsumption = (int) (this.currentSpeedBonusDenominator - 350f);
        int amount_drained = this.getFluidAndDrain(Materials.Thorium.getPlasma(1));

        // allow a 10% margin of error on the optimal plasma consumption
        if (this.maxProgresstime() != 0 && Math.abs(amount_drained - this.currentPlasmaConsumption) <= 0.1f * this.currentPlasmaConsumption) {
            this.currentSpeedBonusDenominator = Math.min(2000f, this.currentSpeedBonusDenominator + 25f);
        }
        // no current recipe or plasma consumed less than 90% of needed
        else if (this.maxProgresstime() == 0 || (amount_drained - this.currentPlasmaConsumption) < -0.1f * this.currentPlasmaConsumption){
            this.currentSpeedBonusDenominator = Math.max(350f, this.currentSpeedBonusDenominator - 3.125f);
        }
        // if plasma consumption is over 110% of needed just do nothing, keep it heated just don't increase or decrease
    }

    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (this.MACHINE_MODE_SPATIAL && aTick % 100 == 0) {
            this.currentParallels = (int) Math.round(this.getBaseParallelRecipes() * this.spatiallyEnlargedConsumptionFormula());
        }
        if (this.MACHINE_MODE_PLASMA && aTick % 10 == 0) {
            this.doPlasmaConsumption();
        }
        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    public float logisticFunction(float a, float b, float c, float d, float x) {
        return (float) (a / (1 + Math.exp(-1 * b * (x - c))) + d);
    }

    @Override
    public int getMaxEfficiency(final ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getPollutionPerSecond(final ItemStack aStack) {
        return CORE.ConfigSwitches.pollutionPerSecondMultiIndustrialCuttingMachine;
    }

    @Override
    public boolean explodesOnComponentBreak(final ItemStack aStack) {
        return false;
    }

    @Override
    public boolean isInputSeparationEnabled() {
        return true;
    }

    public Block getCasingBlock() {
        return ModBlocks.blockCasings2Misc;
    }

    public byte getCasingMeta() {
        return 13;
    }

    public byte getCasingTextureIndex() {
        return (byte) TAE.GTPP_INDEX(29);
    }

    @Override
    public boolean supportsMachineModeSwitch() {
        return true;
    }

    @Override
    public void onModeChangeByScrewdriver(ForgeDirection side, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        setMachineMode(nextMachineMode());
        PlayerUtils.messagePlayer(
            aPlayer,
            String.format(StatCollector.translateToLocal("GT5U.MULTI_MACHINE_CHANGE"), getMachineModeName()));
    }

    @Override
    public void setMachineModeIcons() {
        machineModeIcons.clear();
        machineModeIcons.add(GT_UITextures.OVERLAY_BUTTON_MACHINEMODE_CUTTING);
        machineModeIcons.add(GT_UITextures.OVERLAY_BUTTON_MACHINEMODE_SLICING);
    }

    @Override
    public String getMachineModeName() {
        return StatCollector.translateToLocal("GT5U.GTPP_MULTI_ADV_CUTTING_MACHINE");
    }

//    @Override
//    public void loadNBTData(NBTTagCompound aNBT) {
//        // Migrates old NBT tag to the new one
//        if (aNBT.hasKey("mCuttingMode")) {
//            machineMode = aNBT.getBoolean("mCuttingMode") ? MACHINEMODE_CUTTER : MACHINEMODE_SLICER;
//        }
//        super.loadNBTData(aNBT);
//    }

//    @Override
//    public void getWailaNBTData(EntityPlayerMP player, TileEntity tile, NBTTagCompound tag, World world, int x, int y,
//        int z) {
//        super.getWailaNBTData(player, tile, tag, world, x, y, z);
//        tag.setInteger("mode", machineMode);
//    }
//
//    @Override
//    public void getWailaBody(ItemStack itemStack, List<String> currentTip, IWailaDataAccessor accessor,
//        IWailaConfigHandler config) {
//        super.getWailaBody(itemStack, currentTip, accessor, config);
//        final NBTTagCompound tag = accessor.getNBTData();
//        currentTip.add(
//            StatCollector.translateToLocal("GT5U.machines.oreprocessor1") + " "
//                + EnumChatFormatting.WHITE
//                + StatCollector.translateToLocal("GT5U.GTPP_MULTI_CUTTING_MACHINE.mode." + tag.getInteger("mode"))
//                + EnumChatFormatting.RESET);
//    }
}
