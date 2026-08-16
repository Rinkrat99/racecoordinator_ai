package com.antigravity.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import com.antigravity.context.DatabaseContext;
import com.antigravity.proto.AssetMessage;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class AssetServiceTest {

  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  private DatabaseContext databaseContext;
  private AssetService assetService;
  private String assetsDir;

  @Before
  public void setup() throws Exception {
    String rootDir = tempFolder.newFolder("db_root").getAbsolutePath() + File.separator;
    databaseContext = new DatabaseContext("test_db", null, rootDir);
    assetsDir = tempFolder.newFolder("assets").getAbsolutePath();

    assetService = new AssetService(databaseContext, assetsDir);
  }

  @After
  public void teardown() throws IOException {
    if (databaseContext != null && databaseContext.getConnection() != null) {
      try {
        databaseContext.getConnection().close();
      } catch (Exception ignored) {
      }
    }
  }

  @Test
  public void testGetAssetById_NotFound() {
    AssetMessage asset = assetService.getAssetById("non_existent");
    assertNull(asset);
  }

  @Test
  public void testAssetServiceInitialization() {
    assertNotNull(assetService);
  }

  @Test
  public void testBackfillDefaults() {
    assetService.backfillDefaults();
    assertNotNull(assetService.getAssetById("default_countdown"));
    assertNotNull(assetService.getAssetById("default_seconds_left"));
    assertNotNull(assetService.getAssetById("default_fuel_gauge"));
  }

  @Test
  public void testSaveAssetAndGetAllAssetsAndRenameAndDelete() throws Exception {
    byte[] data = "test sound audio bytes".getBytes(java.nio.charset.StandardCharsets.UTF_8);
    AssetMessage saved = assetService.saveAsset("test_sound.wav", "audio", data);
    assertNotNull(saved);
    org.junit.Assert.assertEquals("test_sound.wav", saved.getName());

    java.util.List<AssetMessage> all = assetService.getAllAssets();
    org.junit.Assert.assertTrue(
        all.stream()
            .anyMatch(a -> a.getModel().getEntityId().equals(saved.getModel().getEntityId())));

    boolean renamed = assetService.renameAsset(saved.getModel().getEntityId(), "renamed_sound.wav");
    org.junit.Assert.assertTrue(renamed);

    AssetMessage updated = assetService.getAssetById(saved.getModel().getEntityId());
    org.junit.Assert.assertEquals("renamed_sound.wav", updated.getName());

    boolean deleted = assetService.deleteAsset(saved.getModel().getEntityId());
    org.junit.Assert.assertTrue(deleted);
    assertNull(assetService.getAssetById(saved.getModel().getEntityId()));
  }

  @Test
  public void testSaveImageSetAndAudioSet() throws Exception {
    com.antigravity.proto.SaveImageSetEntry imgEntry =
        com.antigravity.proto.SaveImageSetEntry.newBuilder()
            .setName("img1.png")
            .setPercentage(50)
            .setUrl("/assets/img1.png")
            .build();

    AssetMessage imgSet =
        assetService.saveImageSet(
            null, "Test Image Set", java.util.Collections.singletonList(imgEntry));
    assertNotNull(imgSet);
    org.junit.Assert.assertEquals("Test Image Set", imgSet.getName());

    com.antigravity.proto.SaveAudioSetEntry audioEntry =
        com.antigravity.proto.SaveAudioSetEntry.newBuilder()
            .setName("countdown.wav")
            .setTimeSeconds(5)
            .setUrl("/assets/countdown.wav")
            .build();

    AssetMessage audioSet =
        assetService.saveAudioSet(
            null, "Test Audio Set", java.util.Collections.singletonList(audioEntry));
    assertNotNull(audioSet);
    org.junit.Assert.assertEquals("Test Audio Set", audioSet.getName());
  }
}
