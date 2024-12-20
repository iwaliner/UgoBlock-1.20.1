package com.iwaliner.ugoblock.object;


import com.iwaliner.ugoblock.mixin.BlockDisplayMixin;
import com.iwaliner.ugoblock.mixin.DisplayMixin;
import com.iwaliner.ugoblock.register.Register;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4d;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class MovingBlockEntity extends Display.BlockDisplay {
    /**移動量を座標で指定。変位なので始点座標でも終点座標でもない。*/
    public static final EntityDataAccessor<BlockPos> DATA_TRANSITION_POSITION_ID = SynchedEntityData.defineId(MovingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
   /**始点座標*/
    public static final EntityDataAccessor<BlockPos> DATA_START_LOCATION_ID = SynchedEntityData.defineId(MovingBlockEntity.class, EntityDataSerializers.BLOCK_POS);
   /**動いているブロックがブロックエンティティを所持していた場合、ここにブロックエンティティのデータが格納される。所持していなかった場合は空のNBTタグが格納される。*/
    public static final EntityDataAccessor<CompoundTag> DATA_BLOCKENTITY_CONTENTS_ID = SynchedEntityData.defineId(MovingBlockEntity.class, EntityDataSerializers.COMPOUND_TAG);
    public static final EntityDataAccessor<Vector3f> DATA_VISUAL_POSITION_ID = SynchedEntityData.defineId(MovingBlockEntity.class, EntityDataSerializers.VECTOR3);
    public static final EntityDataAccessor<Boolean> DATA_ROTATABLE_ID = SynchedEntityData.defineId(MovingBlockEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Byte> DATA_TRIGNOMETRIC_FUNCTION_ID = SynchedEntityData.defineId(MovingBlockEntity.class, EntityDataSerializers.BYTE);

    public MovingBlockEntity(EntityType<?> p_271022_, Level p_270442_) {
        super(Register.MoveableBlock.get(), p_270442_);
        this.noPhysics = false;
        this.noCulling = false;
    }
    public MovingBlockEntity(Level level, BlockPos startPos, BlockState state, int startTick, int duration, BlockPos endPos, BlockEntity blockEntity) {
        super(Register.MoveableBlock.get(), level);
        this.setPos(startPos.getX(),startPos.getY(),startPos.getZ());
        this.entityData.set(BlockDisplayMixin.getData(),state);
        this.entityData.set(DATA_TRANSITION_POSITION_ID,endPos);
        this.entityData.set(DATA_START_LOCATION_ID,startPos);
        this.entityData.set(DisplayMixin.getDataStartTick(),startTick);
        this.entityData.set(DisplayMixin.getDataDuration(),duration);
        this.entityData.set(DATA_TRIGNOMETRIC_FUNCTION_ID,trigonometricFunctionType.NONE.getID());
        this.entityData.set(DATA_VISUAL_POSITION_ID,new Vector3f((float) startPos.getX(),(float)startPos.getY(),(float)startPos.getZ()));
        if(blockEntity!=null) {
            this.entityData.set(DATA_BLOCKENTITY_CONTENTS_ID,blockEntity.saveWithoutMetadata());
        }
        this.entityData.set(DATA_ROTATABLE_ID,false);

        this.noPhysics = false;
        this.noCulling = false;
    }
    public MovingBlockEntity(Level level, BlockPos originPos, BlockPos visualPos, BlockState state, int startTick, int duration, BlockPos endPos, BlockEntity blockEntity,trigonometricFunctionType type) {
        super(Register.MoveableBlock.get(), level);
        this.setPos(originPos.getCenter());
        this.entityData.set(BlockDisplayMixin.getData(),state);
        this.entityData.set(DATA_TRANSITION_POSITION_ID,endPos);
        this.entityData.set(DATA_START_LOCATION_ID,visualPos);
        this.entityData.set(DisplayMixin.getDataStartTick(),startTick);
        this.entityData.set(DisplayMixin.getDataDuration(),duration);
        this.entityData.set(DATA_TRIGNOMETRIC_FUNCTION_ID,type.getID());
        this.entityData.set(DATA_VISUAL_POSITION_ID,new Vector3f((float) visualPos.getX(),(float)visualPos.getY(),(float)visualPos.getZ()));
        if(blockEntity!=null) {
            this.entityData.set(DATA_BLOCKENTITY_CONTENTS_ID,blockEntity.saveWithoutMetadata());
        }
        this.entityData.set(DATA_ROTATABLE_ID,true);
        this.noPhysics = false;
        this.noCulling = false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TRANSITION_POSITION_ID, BlockPos.ZERO);
        this.entityData.define(DATA_START_LOCATION_ID, BlockPos.ZERO);
        this.entityData.define(DATA_BLOCKENTITY_CONTENTS_ID, new CompoundTag());
        this.entityData.define(DATA_VISUAL_POSITION_ID,new Vector3f());
        this.entityData.define(DATA_ROTATABLE_ID,false);
        this.entityData.define(DATA_TRIGNOMETRIC_FUNCTION_ID,trigonometricFunctionType.NONE.getID());
    }

    public void onSyncedDataUpdated(EntityDataAccessor<?> p_277476_) {
        super.onSyncedDataUpdated(p_277476_);
        if (p_277476_.equals(BlockDisplayMixin.getData())) {
            this.updateRenderState = true;
            this.setBoundingBox(this.makeBoundingBox());

        }
    }
    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("transition")) {
            this.entityData.set(DATA_TRANSITION_POSITION_ID, NbtUtils.readBlockPos(tag.getCompound("transition")));
        }
        if (tag.contains("start_location")) {
            this.entityData.set(DATA_START_LOCATION_ID, NbtUtils.readBlockPos(tag.getCompound("start_location")));
        }
      if (tag.contains("BlockEntityTag")) {
            this.entityData.set(DATA_BLOCKENTITY_CONTENTS_ID,tag.getCompound("BlockEntityTag"));
        }
        if (tag.contains("tickCount")) {
            this.tickCount=tag.getInt("tickCount");
        }
        if (tag.contains("visual_position_x")&&tag.contains("visual_position_y")&&tag.contains("visual_position_z")) {
            this.entityData.set(DATA_VISUAL_POSITION_ID, new Vector3f(tag.getFloat("visual_position_x"),tag.getFloat("visual_position_y"),tag.getFloat("visual_position_z")));
        }
        if (tag.contains("rotatable")) {
            this.entityData.set(DATA_ROTATABLE_ID,tag.getBoolean("rotatable"));
        }
        if (tag.contains("trigonometric_function_type")) {
            this.entityData.set(DATA_TRIGNOMETRIC_FUNCTION_ID,tag.getByte("trigonometric_function_type"));
        }
    }
    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("transition",NbtUtils.writeBlockPos(entityData.get(DATA_TRANSITION_POSITION_ID)));
        tag.put("start_location",NbtUtils.writeBlockPos(entityData.get(DATA_START_LOCATION_ID)));
        tag.putInt("tickCount",tickCount);
         if(!entityData.get(DATA_BLOCKENTITY_CONTENTS_ID).isEmpty()) {
            tag.put("BlockEntityTag",entityData.get(DATA_BLOCKENTITY_CONTENTS_ID));
        }
         tag.putFloat("visual_position_x", (float) getVisualPos().x);
        tag.putFloat("visual_position_y",(float)getVisualPos().y);
        tag.putFloat("visual_position_z",(float)getVisualPos().z);
        tag.putBoolean("rotatable",entityData.get(DATA_ROTATABLE_ID));
        tag.putByte("trigonometric_function_type",entityData.get(DATA_TRIGNOMETRIC_FUNCTION_ID));

    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return true;
    }
    public boolean canBeCollidedWith() {
        return true;
    }


    public boolean isPickable() {
        return true;
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        super.makeBoundingBox();
     BlockState state=getState();
     VoxelShape shape=state.getCollisionShape(level(),this.blockPosition());
     double [] d0=shouldRotate()? new double[]{getDisplacementVisualDuringRotation().x, getDisplacementVisualDuringRotation().y, getDisplacementVisualDuringRotation().z} : new double[]{0D, 0D, 0D};
     AABB aabb=new AABB(position().x+shape.min(Direction.Axis.X)+d0[0],position().y+shape.min(Direction.Axis.Y)+d0[1],position().z+shape.min(Direction.Axis.Z)+d0[2],position().x+shape.max(Direction.Axis.X)+d0[0],position().y+shape.max(Direction.Axis.Y)+d0[1],position().z+shape.max(Direction.Axis.Z)+d0[2]);

        return aabb;
    }
    @Override
    public @NotNull AABB getBoundingBoxForCulling() {
        return makeBoundingBox();
    }
    @Override
    public void tick() {
        if(getState().isAir()){
            discard();
        }
        super.tick();
        setBoundingBox(makeBoundingBox());
        refreshDimensions();
        BlockPos transition= getTransition();
        int duration=getDuration();
        int startTick=getStartTick();
        if(!shouldRotate()) {
            if (duration > 0 && tickCount >= startTick && tickCount < startTick + duration) {
                Vec3 pos = new Vec3((double) getVisualPos().x + (double) transition.getX() / (double) duration, (double) getVisualPos().y + (double) transition.getY() / (double) duration, (double) getVisualPos().z + (double) transition.getZ() / (double) duration);
                setVisualPos(pos);
                setActualPos(pos);
                for (Entity entity : level().getEntities((Entity) null, getBoundingBoxForCulling().move(0.5D, 0.5D, 0.5D).inflate(0d, 0.1d, 0d), (o) -> {
                    return !(o instanceof MovingBlockEntity);
                })) {
                    if (entity instanceof Player) { /**移動中のブロックに乗っているプレイヤーが動けなくなるのはまずいので、他エンティティと処理を分けてる*/
                        double endY = 0D;
                        if (transition.getY() > 0D) {
                            endY = 0.2D;
                        } else if (transition.getY() < 0D) {
                        }
                        List<MovingBlockEntity> colliedEntityList = level().getEntitiesOfClass(MovingBlockEntity.class, entity.getBoundingBox().inflate(0D, 0.1D, 0D));
                        int collidedAmount = colliedEntityList.size();
                        if (collidedAmount != 0) {
                            Vec3 entityPos = new Vec3((double) entity.position().x + ((double) transition.getX() / (double) duration) / (double) collidedAmount, entity.position().y+0.1D+ ((double) transition.getY() / (double) (duration)) / (double) collidedAmount, (double) entity.position().z + ((double) transition.getZ() / (double) duration) / (double) collidedAmount);
                            entity.setPos(entityPos);
                        }
                        if (tickCount == startTick + duration - 1) {
                          //  entity.setPos(entity.position().add(0D, endY, 0D));
                        }
                    } else { /**プレイヤーは上のテレポートによる移動でかくつかなかったが、他のエンティティはこれではかくついてしまうので、エンティティに速度を持たせて移動させてる。ただし身動きがとれなくなる。*/
                        Vec3 entityPos = new Vec3((double) entity.position().x, entity.position().y, entity.position().z);
                        entity.setPos(entityPos);
                        entity.fallDistance=0f;
                        Vec3 speed = new Vec3((double) transition.getX() / (double) duration, (double) transition.getY() / (double) duration, (double) transition.getZ() / (double) duration);
                        entity.setDeltaMovement(speed);
                        entity.setOnGround(true);
                    }
                }
            } else if (duration > 0 && tickCount == startTick + duration + 0) {
                makeBlock();
            } else if (duration > 0 && tickCount == startTick + duration + 1) {
                discard();
            }
        }else{
            rotate();
        }
    }
    public boolean shouldFixFighting(){ /**このエンティティとブロックが完全に重なりZ-fightingを起こす可能性があるかどうか*/
        return (getDuration()>0&&tickCount>getStartTick()+getDuration()-0)||tickCount<2;
    }
    private void makeBlock(){ /**移動し終わってブロック化する*/
        if(!level().isClientSide) {
            BlockPos pos = new BlockPos(getStartLocation().getX() + getTransition().getX(), getStartLocation().getY() + getTransition().getY(), getStartLocation().getZ() + getTransition().getZ());
            BlockState movingState = getState();
            if (level().getBlockState(pos).canBeReplaced()) {
               if(movingState.getBlock()==Blocks.OBSERVER){
                    level().setBlock(pos, movingState,82);
                    level().scheduleTick(pos, movingState.getBlock(), 2);
                  }else {
                   level().setBlock(pos, movingState, 82);
                   level().scheduleTick(pos, movingState.getBlock(), 2);
               }
                if (!getBlockEntityData().isEmpty() && movingState.hasBlockEntity()) {
                    CompoundTag compoundtag = getBlockEntityData();
                    if (compoundtag != null) {
                        BlockEntity blockentity = level().getBlockEntity(pos);

                        if (blockentity != null) {
                            blockentity.load(getBlockEntityData());
                        }
                    }
                }
            } else { /**移動してきた場所が他のブロックで埋まっていた場合。アイテム化する。*/
                if (!level().isClientSide&&!movingState.is(Register.TAG_DISABLE_ITEM_DROP)) { /**通常*/
                    ItemEntity itemEntity = new ItemEntity(level(), position().x, position().y, position().z, new ItemStack(movingState.getBlock()));
                    level().addFreshEntity(itemEntity);
                }else if(!level().getBlockState(pos).is(Register.TAG_DISABLE_ITEM_DROP)){ /**アイテムをドロップしたくないブロックが移動してきたがその場所が埋まっていた場合。もともとあったブロックをアイテム化したうえでドロップしたくないブロックを設置する。*/
                    ItemEntity itemEntity = new ItemEntity(level(), position().x, position().y, position().z, new ItemStack(level().getBlockState(pos).getBlock()));
                    level().addFreshEntity(itemEntity);
                    level().setBlock(pos,movingState,82);
                }
                discard();
            }
        }
    }

    public void setVisualPos(Vector3f visualPos){
        entityData.set(DATA_VISUAL_POSITION_ID,visualPos);
    }
    public void setActualPos(Vector3f vector3f){
        setPos(vector3f.x,vector3f.y,vector3f.z);
    }
    public Vec3 getVisualPos(){
        return new Vec3(entityData.get(DATA_VISUAL_POSITION_ID).x,entityData.get(DATA_VISUAL_POSITION_ID).y,entityData.get(DATA_VISUAL_POSITION_ID).z);
    }
    public void setVisualPos(Vec3 visualPos){
        entityData.set(DATA_VISUAL_POSITION_ID,visualPos.toVector3f());
    }
    public Vec3 getActualPos(){
        return position();
    }
    public void setActualPos(Vec3 vec3){
        setPos(vec3.x,vec3.y,vec3.z);
    }
    public BlockPos getVisualBlockPos(){
        return new BlockPos(Mth.floor(entityData.get(DATA_VISUAL_POSITION_ID).x),Mth.floor(entityData.get(DATA_VISUAL_POSITION_ID).y),Mth.floor(entityData.get(DATA_VISUAL_POSITION_ID).z));
    }
    public void setVisualBlockPos(BlockPos visualPos){
        entityData.set(DATA_VISUAL_POSITION_ID,visualPos.getCenter().toVector3f());
    }
    public BlockPos getActualBlockPos(){
        return new BlockPos(Mth.floor(getActualPos().x),Mth.floor(getActualPos().y),Mth.floor(getActualPos().z));
    }
    public void setActualBlockPos(BlockPos blockPos){
        setPos(blockPos.getCenter());
    }
    public Vec3 getDisplacementVisual(){
        return new Vec3(getVisualPos().x-getActualPos().x,getVisualPos().y-getActualPos().y,getVisualPos().z-getActualPos().z);
    }
    public float getVisualDistance(){
        return Mth.sqrt((float) (Mth.square(getDisplacementVisual().x+0.5D)+Mth.square(getDisplacementVisual().y+0.5D)+Mth.square(getDisplacementVisual().z+0.5D)));
    }
    public Vec3 getDisplacementVisualDuringRotation(){

        int time=tickCount-getStartTick();
        if(tickCount>getStartTick()+getDuration()){
            time=getStartTick()+getDuration();
        }
        float[] rotationPos;
        float firstAngle;
        switch (getTrigonometricFunctionType()){
            case Y_COUNTERCLOCKWISE ->firstAngle= (float) Math.atan2(getDisplacementVisual().x+0.5D,getDisplacementVisual().z+0.5D);
            default-> firstAngle= 0f;
        }
        float finalAngle=getLeftRotation().angle();
        float radian=firstAngle+time*(finalAngle)/(float)getDuration();
        switch (getTrigonometricFunctionType()){
            case Y_COUNTERCLOCKWISE -> rotationPos= new float[]{(float) Math.sin(radian), 0f, (float) Math.cos(radian)};
            default-> rotationPos= new float[]{0f, 0f, 0f};
        }


        return new Vec3(rotationPos[0]*getVisualDistance()-0.5D,rotationPos[1]*getVisualDistance()-0.5D,rotationPos[2]*getVisualDistance()-0.5D);
    }
    public BlockState getState(){
        return entityData.get(BlockDisplayMixin.getData());
    }
    public void setState(BlockState state){
         entityData.set(BlockDisplayMixin.getData(),state);
    }
    private int getStartTick(){
        return entityData.get(DisplayMixin.getDataStartTick());
    }
    private BlockPos getTransition(){
        return entityData.get(DATA_TRANSITION_POSITION_ID);
    }
    private BlockPos getStartLocation(){
        return entityData.get(DATA_START_LOCATION_ID);
    }
    private CompoundTag getBlockEntityData(){
        return entityData.get(DATA_BLOCKENTITY_CONTENTS_ID);
    }
    private Quaternionf getLeftRotation(){
        return entityData.get(DisplayMixin.getDataLeftRotation());
    }

    private Quaternionf getRightRotation(){
        return entityData.get(DisplayMixin.getDataRightRotation());
    }
    private void setLeftRotation(Quaternionf quaternionf){
        entityData.set(DisplayMixin.getDataLeftRotation(),quaternionf);
    }
    private void setRightRotation(Quaternionf quaternionf){
        entityData.set(DisplayMixin.getDataRightRotation(),quaternionf);
    }
    private void setDuration(int duration){
        entityData.set(DisplayMixin.getDataDuration(),duration);
    }
    private int getDuration(){
        return entityData.get(DisplayMixin.getDataDuration());
    }
    public boolean shouldRotate(){
        return entityData.get(DATA_ROTATABLE_ID);
    }

    private trigonometricFunctionType getTrigonometricFunctionType(){
       return trigonometricFunctionType.getType(entityData.get(DATA_TRIGNOMETRIC_FUNCTION_ID));
    }
    private void setTrigonometricFunctionType(trigonometricFunctionType type){
        entityData.set(DATA_TRIGNOMETRIC_FUNCTION_ID,type.getID());
    }

    @Override
    public boolean mayInteract(Level level, BlockPos poa) {
        return true;
    }

    @Override
    public InteractionResult interactAt(Player player, Vec3 vec3, InteractionHand hand) {
        BlockState state=getState();
        if(state.getBlock() instanceof FenceGateBlock||state.getBlock() instanceof TrapDoorBlock){
             level().playSound(player, blockPosition(), state.getValue(BlockStateProperties.OPEN) ? SoundEvents.FENCE_GATE_OPEN : SoundEvents.FENCE_GATE_CLOSE, SoundSource.BLOCKS, 1.0F, level().getRandom().nextFloat() * 0.1F + 0.9F);
            setState(state.cycle(BlockStateProperties.OPEN));

            return InteractionResult.SUCCESS;
        }

       return super.interactAt(player, vec3, hand);
    }

    private  void rotate(){
       /*// setYRot(tickCount*0.1f);
        int durationRotation=200;
        float rotationDegree=90f;
        float degreePerTick=(rotationDegree/(float) durationRotation)*Mth.PI/180f;
        int tx=0;
        int tz=-3;
        final double x0= position().x;
        final double z0= position().z;
        double x=position().x;
        double y=position().x;
        double z= position().z;
        double centerX=x0+tx;
        double centerZ=z0+tz;
        float distanceFromCenter=Mth.sqrt(tx^2+tz^2);
        int rotationTick=Mth.floor(tickCount/(double)durationRotation);
        float degree= (float) ((Math.atan((x-x0)/(z-z0)))*180F/Mth.PI);
        if(tickCount<=durationRotation) {
          //  this.teleportRelative();
         //   moveTo(x,y,z-0.1D);
            this.moveTo(x,y,z, getYRot()-degreePerTick, 0f);

            // setYRot(getYRot()-degreePerTick);
           // this.moveTo(centerX+distanceFromCenter*Mth.sin(degreePerTick+degree), position().y(), centerZ+distanceFromCenter*Mth.cos(degreePerTick+degree), getYRot()-degreePerTick, 0f);
        }*/
        //setYRot(getYRot()-5f);
       // moveTo(new Vec3(-60, -60,26));
        setLeftRotation(new Quaternionf(new AxisAngle4d(1.57D,0D,1D,0D)));
        setRightRotation(new Quaternionf(new AxisAngle4d(0D,0D,1D,0D)));
        setDuration(200);
    }
    public  enum trigonometricFunctionType {
        NONE((byte) 0),
        X_COUNTERCLOCKWISE((byte) 1),
        X_CLOCKWISE((byte) 2),

        Y_COUNTERCLOCKWISE((byte) 3),
        Y_CLOCKWISE((byte) 4),
        Z_COUNTERCLOCKWISE((byte) 5),
        Z_CLOCKWISE((byte) 6);

        private byte id;

        private trigonometricFunctionType(byte id) {
            this.id = id;
        }
        public static trigonometricFunctionType getType(byte id){
            return switch (id) {
                case 0 -> NONE;
                case 1 -> X_COUNTERCLOCKWISE;
                case 2 -> X_CLOCKWISE;
                case 3 -> Y_COUNTERCLOCKWISE;
                case 4 -> Y_CLOCKWISE;
                case 5 -> Z_COUNTERCLOCKWISE;
                case 6 -> Z_CLOCKWISE;
                default -> NONE;
            };

        }
        public byte getID()
        {
            return this.id;
        }
    }
}
