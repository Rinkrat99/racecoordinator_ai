package com.antigravity.handlers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.antigravity.context.DatabaseContext;
import com.antigravity.proto.AssetMessage;
import com.antigravity.service.AssetService;
import io.javalin.Javalin;
import io.javalin.http.Context;
import java.io.File;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssetTaskHandlerTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private DatabaseContext databaseContext;
  private Javalin app;
  private AssetTaskHandler handler;
  private Context ctx;

  @Before
  public void setUp() throws Exception {
    String rootDir = tempFolder.newFolder("db_root").getAbsolutePath() + File.separator;
    databaseContext = new DatabaseContext("TestDB", null, rootDir);
    app = mock(Javalin.class);
    ctx = mock(Context.class);

    AssetService assetService = mock(AssetService.class);
    handler = org.mockito.Mockito.spy(new AssetTaskHandler(databaseContext, app));
    org.mockito.Mockito.doReturn(assetService).when(handler).getAssetService();

    org.mockito.Mockito.doNothing().when(handler).setStatus(any(), anyInt());
    org.mockito.Mockito.doNothing().when(handler).setResult(any(), anyString());
    org.mockito.Mockito.doNothing().when(handler).setResult(any(), any(byte[].class));
    org.mockito.Mockito.doNothing().when(handler).setStream(any(), any());
    org.mockito.Mockito.doNothing().when(handler).setContentType(any(), anyString());
    org.mockito.Mockito.doReturn("dummy").when(handler).getPathParam(any(), anyString());
    org.mockito.Mockito.doReturn(new byte[0]).when(handler).getBodyBytes(any());
  }

  @After
  public void tearDown() {
    if (databaseContext != null && databaseContext.getConnection() != null) {
      try {
        databaseContext.getConnection().close();
      } catch (Exception ignored) {
      }
    }
  }

  @Test
  public void testDownloadAsset_ImageSetFallback() throws Exception {
    String assetId = "set-123";
    org.mockito.Mockito.doReturn(assetId).when(handler).getPathParam(ctx, "id");

    AssetService assetService = handler.getAssetService();
    when(assetService.getAssetById(assetId))
        .thenReturn(
            AssetMessage.newBuilder().setType("image_set").setUrl("/assets/thumb_123.png").build());

    File assetsDir = new File(databaseContext.getDataRoot() + "TestDB/assets");
    assetsDir.mkdirs();
    new File(assetsDir, "thumb_123.png").createNewFile();

    handler.downloadAsset(ctx);

    verify(handler, org.mockito.Mockito.never()).setStatus(eq(ctx), eq(404));
    verify(handler).setStream(eq(ctx), any());
  }

  @Test
  public void testSaveAudioSet() throws Exception {
    com.antigravity.proto.SaveAudioSetRequest request =
        com.antigravity.proto.SaveAudioSetRequest.newBuilder().setName("My Audio Set").build();
    org.mockito.Mockito.doReturn(request.toByteArray()).when(handler).getBodyBytes(any());

    AssetService assetService = handler.getAssetService();
    when(assetService.saveAudioSet(any(), anyString(), any()))
        .thenReturn(AssetMessage.newBuilder().build());

    handler.saveAudioSet(ctx);
    verify(handler).setResult(any(), any(byte[].class));
  }

  private void invoke(AssetTaskHandler handler, String methodName, Context ctx) throws Exception {
    java.lang.reflect.Method m =
        AssetTaskHandler.class.getDeclaredMethod(methodName, Context.class);
    m.setAccessible(true);
    m.invoke(handler, ctx);
  }

  @Test
  public void testListAssets() throws Exception {
    AssetService assetService = handler.getAssetService();
    when(assetService.getAllAssets()).thenReturn(java.util.Collections.emptyList());

    invoke(handler, "listAssets", ctx);
    verify(handler).setResult(eq(ctx), any(byte[].class));
  }

  @Test
  public void testUploadAsset() throws Exception {
    com.antigravity.proto.UploadAssetRequest req =
        com.antigravity.proto.UploadAssetRequest.newBuilder()
            .setName("sample.png")
            .setType("image")
            .setData(com.google.protobuf.ByteString.copyFromUtf8("fake_image_bytes"))
            .build();
    org.mockito.Mockito.doReturn(req.toByteArray()).when(handler).getBodyBytes(any());

    AssetService assetService = handler.getAssetService();
    when(assetService.saveAsset(anyString(), anyString(), any()))
        .thenReturn(AssetMessage.newBuilder().setName("sample.png").build());

    invoke(handler, "uploadAsset", ctx);
    verify(handler).setResult(eq(ctx), any(byte[].class));
  }

  @Test
  public void testDeleteAsset() throws Exception {
    com.antigravity.proto.DeleteAssetRequest req =
        com.antigravity.proto.DeleteAssetRequest.newBuilder().setId("asset_123").build();
    org.mockito.Mockito.doReturn(req.toByteArray()).when(handler).getBodyBytes(any());

    AssetService assetService = handler.getAssetService();
    when(assetService.deleteAsset("asset_123")).thenReturn(true);

    handler.deleteAsset(ctx);
    verify(handler).setResult(eq(ctx), any(byte[].class));
  }

  @Test
  public void testRenameAsset() throws Exception {
    com.antigravity.proto.RenameAssetRequest req =
        com.antigravity.proto.RenameAssetRequest.newBuilder()
            .setId("asset_123")
            .setNewName("renamed.png")
            .build();
    org.mockito.Mockito.doReturn(req.toByteArray()).when(handler).getBodyBytes(any());

    AssetService assetService = handler.getAssetService();
    when(assetService.renameAsset("asset_123", "renamed.png")).thenReturn(true);

    invoke(handler, "renameAsset", ctx);
    verify(handler).setResult(eq(ctx), any(byte[].class));
  }

  @Test
  public void testSaveImageSet() throws Exception {
    com.antigravity.proto.SaveImageSetRequest req =
        com.antigravity.proto.SaveImageSetRequest.newBuilder().setName("Sample Set").build();
    org.mockito.Mockito.doReturn(req.toByteArray()).when(handler).getBodyBytes(any());

    AssetService assetService = handler.getAssetService();
    when(assetService.saveImageSet(any(), anyString(), any()))
        .thenReturn(AssetMessage.newBuilder().build());

    invoke(handler, "saveImageSet", ctx);
    verify(handler).setResult(eq(ctx), any(byte[].class));
  }

  @Test
  public void testSaveCustomRotation() throws Exception {
    com.antigravity.proto.SaveCustomRotationRequest req =
        com.antigravity.proto.SaveCustomRotationRequest.newBuilder()
            .setName("4-Lane Rotation")
            .build();
    org.mockito.Mockito.doReturn(req.toByteArray()).when(handler).getBodyBytes(any());

    AssetService assetService = handler.getAssetService();
    when(assetService.saveCustomRotation(
            any(), anyString(), org.mockito.ArgumentMatchers.anyInt(), any()))
        .thenReturn(AssetMessage.newBuilder().build());

    invoke(handler, "saveCustomRotation", ctx);
    verify(handler).setResult(eq(ctx), any(byte[].class));
  }

  @Test
  public void testServeFile_DirectoryTraversalBlocked() throws Exception {
    org.mockito.Mockito.doReturn("../secret.txt").when(handler).getPathParam(ctx, "filename");

    invoke(handler, "serveAsset", ctx);
    verify(handler).setStatus(eq(ctx), eq(403));
  }

  @Test
  public void testServeFile_NotFound() throws Exception {
    org.mockito.Mockito.doReturn("nonexistent.png").when(handler).getPathParam(ctx, "filename");

    invoke(handler, "serveAsset", ctx);
    verify(handler).setStatus(eq(ctx), eq(404));
  }
}
