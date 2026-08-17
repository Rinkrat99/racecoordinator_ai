const { test, describe } = require('node:test');
const assert = require('node:assert');
const { determineVersion } = require('./determine_release_version');

describe('determine_release_version', () => {
  test('should calculate daily alpha for schedule event', () => {
    const res = determineVersion('schedule', 'refs/heads/develop', '');
    assert.match(res.version, /^0\.0\.0-alpha\.\d{8}$/);
    assert.strictEqual(res.tag, `v${res.version}`);
    assert.strictEqual(res.isPrerelease, 'true');
  });

  test('should use explicit tag push when tag is provided', () => {
    const res1 = determineVersion('push', 'refs/tags/v1.0.0', '');
    assert.strictEqual(res1.version, '1.0.0');
    assert.strictEqual(res1.tag, 'v1.0.0');
    assert.strictEqual(res1.isPrerelease, 'false');

    const res2 = determineVersion('push', 'refs/tags/v1.0.0-beta.3', '');
    assert.strictEqual(res2.version, '1.0.0-beta.3');
    assert.strictEqual(res2.tag, 'v1.0.0-beta.3');
    assert.strictEqual(res2.isPrerelease, 'true');
  });

  test('should handle manual workflow_dispatch override', () => {
    const res = determineVersion('workflow_dispatch', 'refs/heads/main', '1.5.0-beta.2');
    assert.strictEqual(res.version, '1.5.0-beta.2');
    assert.strictEqual(res.tag, 'v1.5.0-beta.2');
    assert.strictEqual(res.isPrerelease, 'true');
  });

  describe('push to release branch (beta releases)', () => {
    test('should start at beta.1 if no existing beta tags', () => {
      const res = determineVersion('push', 'refs/heads/release/v1.0.0', '', [], '0.0.0');
      assert.strictEqual(res.version, '1.0.0-beta.1');
      assert.strictEqual(res.tag, 'v1.0.0-beta.1');
      assert.strictEqual(res.isPrerelease, 'true');
    });

    test('should increment beta number when beta tags exist', () => {
      const existingTags = ['v1.0.0-beta.1', 'v1.0.0-beta.2', 'v0.9.0'];
      const res = determineVersion('push', 'refs/heads/release/v1.0.0', '', existingTags, '0.0.0');
      assert.strictEqual(res.version, '1.0.0-beta.3');
      assert.strictEqual(res.tag, 'v1.0.0-beta.3');
      assert.strictEqual(res.isPrerelease, 'true');
    });

    test('should handle multi-digit beta numbers correctly (e.g. beta.9 -> beta.10)', () => {
      const existingTags = ['v1.0.0-beta.1', 'v1.0.0-beta.9'];
      const res = determineVersion('push', 'refs/heads/release/v1.0.0', '', existingTags, '0.0.0');
      assert.strictEqual(res.version, '1.0.0-beta.10');
      assert.strictEqual(res.tag, 'v1.0.0-beta.10');
      assert.strictEqual(res.isPrerelease, 'true');
    });

    test('should handle release branches without leading v and short versions (e.g. release/1.0)', () => {
      const res = determineVersion('push', 'refs/heads/release/1.0', '', [], '0.0.0');
      assert.strictEqual(res.version, '1.0.0-beta.1');
      assert.strictEqual(res.tag, 'v1.0.0-beta.1');
    });
  });

  describe('push to main (official production releases)', () => {
    test('should start at 0.0.0 when VERSION is 0.0.0 and no tags exist', () => {
      const res = determineVersion('push', 'refs/heads/main', '', [], '0.0.0');
      assert.strictEqual(res.version, '0.0.0');
      assert.strictEqual(res.tag, 'v0.0.0');
      assert.strictEqual(res.isPrerelease, 'false');
    });

    test('should increment patch for 0.0.x when 0.0.0 exists', () => {
      const existingTags = ['v0.0.0'];
      const res = determineVersion('push', 'refs/heads/main', '', existingTags, '0.0.0');
      assert.strictEqual(res.version, '0.0.1');
      assert.strictEqual(res.tag, 'v0.0.1');
      assert.strictEqual(res.isPrerelease, 'false');
    });

    test('should release 1.0.0 when VERSION is updated to 1.0 or 1.0.0 with older tags existing', () => {
      const existingTags = ['v0.0.0', 'v0.0.1', 'v1.0.0-beta.1', 'v1.0.0-beta.2'];
      const res = determineVersion('push', 'refs/heads/main', '', existingTags, '1.0');
      assert.strictEqual(res.version, '1.0.0');
      assert.strictEqual(res.tag, 'v1.0.0');
      assert.strictEqual(res.isPrerelease, 'false');
    });

    test('should increment patch to 1.0.1 on next push to main when v1.0.0 exists', () => {
      const existingTags = ['v1.0.0', 'v1.0.0-beta.1'];
      const res = determineVersion('push', 'refs/heads/main', '', existingTags, '1.0');
      assert.strictEqual(res.version, '1.0.1');
      assert.strictEqual(res.tag, 'v1.0.1');
      assert.strictEqual(res.isPrerelease, 'false');
    });

    test('should increment patch to 1.0.2 when v1.0.0 and v1.0.1 exist', () => {
      const existingTags = ['v1.0.0', 'v1.0.1'];
      const res = determineVersion('push', 'refs/heads/main', '', existingTags, '1.0.0');
      assert.strictEqual(res.version, '1.0.2');
      assert.strictEqual(res.tag, 'v1.0.2');
      assert.strictEqual(res.isPrerelease, 'false');
    });

    test('should reset patch to 0 when VERSION is bumped to 1.1', () => {
      const existingTags = ['v1.0.0', 'v1.0.1', 'v1.0.2'];
      const res = determineVersion('push', 'refs/heads/main', '', existingTags, '1.1');
      assert.strictEqual(res.version, '1.1.0');
      assert.strictEqual(res.tag, 'v1.1.0');
      assert.strictEqual(res.isPrerelease, 'false');
    });
  });
});
