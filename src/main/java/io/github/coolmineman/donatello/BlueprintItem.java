package io.github.coolmineman.donatello;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.coolmineman.bitsandchisels.BitItem;
import io.github.coolmineman.bitsandchisels.api.BitUtils;
import io.github.coolmineman.bitsandchisels.api.client.RedBoxCallback;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlueprintItem extends Item {

    public static final Identifier PACKET_ID = new Identifier("donatello", "blueprint_place");

    private static List<Pattern> patterns = new ArrayList<>(); 

    static {
        boolean[][][] slab = new boolean[16][8][16];
        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 16; k++) {
                    slab[i][j][k] = true;
                }
            }
        }
        patterns.add(new Pattern(slab));
        ServerSidePacketRegistry.INSTANCE.register(PACKET_ID, (packetContext, attachedData) -> {
            BlockPos pos = attachedData.readBlockPos();
            int x = 0;
            int y = attachedData.readInt();
            int z = 0;
            packetContext.getTaskQueue().execute(() -> {
                // Execute on the main thread
                PlayerEntity player = packetContext.getPlayer();
                World world = player.world;
                ItemStack stack = player.getMainHandStack();
                int blueprint = getBlueprintId(player.getStackInHand(Hand.MAIN_HAND));
                if (blueprint < 0) return;
                Pattern pattern = patterns.get(blueprint);
                boolean[][][] pattern2 = pattern.pattern;
                if (world.canSetBlock(pos) && stack.getItem() == Donatello.BLUEPRINT && player.getBlockPos().getSquaredDistance(pos.getX(), pos.getY(), pos.getZ(), true) < 81) {
                    for (int i = 0; i < pattern2.length; i++) {
                        for (int j = 0; j < pattern2[0].length; j++) {
                            for (int k = 0; k < pattern2[0][0].length; k++) {
                                if (world.canSetBlock(pos) && player.getBlockPos().getSquaredDistance(pos.getX(), pos.getY(), pos.getZ(), true) < 81/* && !BitUtils.exists(BitUtils.getBit(world, pos, x, y, z))*/) {
                                    ItemStack stack2 = player.getStackInHand(Hand.OFF_HAND);
                                    if (!stack2.isEmpty() && stack2.getItem() instanceof BitItem) {
                                        boolean b = BitUtils.setBit(world, pos, x + i, y + j, z + k, BitUtils.getBit(stack2));
                                        if (b && !player.isCreative()) stack2.decrement(1);
                                    } else {
                                        BitUtils.update(world, pos);
                                        return;
                                    }
                                }
                            }
                        }
                    }
                    BitUtils.update(world, pos);
                }
            });
        });
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            initClient();
        }
    }

    public static void initClient() {
        RedBoxCallback.EVENT.register((redBoxDrawer, matrixStack, vertexConsumer, worldoffsetx, worldoffsety, worldoffsetz) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            int blueprint = getBlueprintId(client.player.getMainHandStack());
            if (blueprint >= 0) {
                HitResult hit = client.crosshairTarget;
                if (hit.getType() == HitResult.Type.BLOCK) {
                    Direction direction = ((BlockHitResult)hit).getSide();
                    BlockPos pos = ((BlockHitResult)hit).getBlockPos();
                    int x = ((int) Math.floor(((hit.getPos().getX() - pos.getX()) * 16) + (direction.getOffsetX() * -0.5d))) + direction.getOffsetX();
                    int y = ((int) Math.floor(((hit.getPos().getY() - pos.getY()) * 16) + (direction.getOffsetY() * -0.5d))) + direction.getOffsetY();
                    int z = ((int) Math.floor(((hit.getPos().getZ() - pos.getZ()) * 16) + (direction.getOffsetZ() * -0.5d))) + direction.getOffsetZ();

                    if (x > 15) {
                        pos = pos.add(1, 0, 0);
                        x -= 16;
                    }
                    if (y > 15) {
                        pos = pos.add(0, 1, 0);
                        y -= 16;
                    }
                    if (z > 15) {
                        pos = pos.add(0, 0, 1);
                        z -= 16;
                    }
                    if (x < 0) {
                        pos = pos.add(-1, 0, 0);
                        x += 16;
                    }
                    if (y < 0) {
                        pos = pos.add(0, -1, 0);
                        y += 16;
                    }
                    if (z < 0) {
                        pos = pos.add(0, 0, -1);
                        z += 16;
                    }

                    boolean[][][] pattern = patterns.get(blueprint).pattern;
                    int yoffset = pattern[0].length == 8 ? (y < 8 ? 0 : 8) : 0;

                    for (int i = 0; i < 16; i++) {
                        for (int j = 0; j < pattern[0].length; j++) {
                            for (int k = 0; k < 16; k++) {
                                if (pattern[i][j][k]) redBoxDrawer.drawRedBox(matrixStack, vertexConsumer, pos, i, yoffset + j, k, worldoffsetx, worldoffsety, worldoffsetz);
                            }
                        }
                    }
                }
            }
        });
    }

    public static int getBlueprintId(ItemStack stack) {
        if (stack.getItem() instanceof BlueprintItem) {
            return 0;
        }
        return -1;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        int blueprint = getBlueprintId(context.getPlayer().getStackInHand(Hand.MAIN_HAND));
        if (blueprint < 0) return ActionResult.PASS;
        MinecraftClient client = MinecraftClient.getInstance();
        HitResult hit = client.crosshairTarget;
        BlockPos pos = context.getBlockPos();

        if (hit.getType() == HitResult.Type.BLOCK) {
            Direction direction = ((BlockHitResult)hit).getSide();
            int x = ((int) Math.floor(((hit.getPos().getX() - pos.getX()) * 16) + (direction.getOffsetX() * -0.5d))) + direction.getOffsetX();
            int y = ((int) Math.floor(((hit.getPos().getY() - pos.getY()) * 16) + (direction.getOffsetY() * -0.5d))) + direction.getOffsetY();
            int z = ((int) Math.floor(((hit.getPos().getZ() - pos.getZ()) * 16) + (direction.getOffsetZ() * -0.5d))) + direction.getOffsetZ();

            if (x > 15) {
                pos = pos.add(1, 0, 0);
                x -= 16;
            }
            if (y > 15) {
                pos = pos.add(0, 1, 0);
                y -= 16;
            }
            if (z > 15) {
                pos = pos.add(0, 0, 1);
                z -= 16;
            }
            if (x < 0) {
                pos = pos.add(-1, 0, 0);
                x += 16;
            }
            if (y < 0) {
                pos = pos.add(0, -1, 0);
                y += 16;
            }
            if (z < 0) {
                pos = pos.add(0, 0, -1);
                z += 16;
            }

            PacketByteBuf passedData = new PacketByteBuf(Unpooled.buffer());
            passedData.writeBlockPos(pos);
            passedData.writeInt(patterns.get(blueprint).pattern[0].length == 8 ? (y < 8 ? 0 : 8) : 0);
            ClientSidePacketRegistry.INSTANCE.sendToServer(PACKET_ID, passedData);
            return ActionResult.CONSUME;
        }
        return ActionResult.PASS;
    }

    public BlueprintItem(Settings settings) {
        super(settings);
    }

    public static class Pattern {
        public final boolean[][][] pattern;
        public final int cost;
        public Pattern(boolean[][][] pattern) {
            this.pattern = pattern;
            int cost = 0;
            for (int i = 0; i < pattern.length; i++) {
                for (int j = 0; j < pattern[0].length; j++) {
                    for (int k = 0; k < pattern[0][0].length; k++) {
                        cost++;
                    }
                }
            }
            this.cost = cost;
        }
    }
    
}
