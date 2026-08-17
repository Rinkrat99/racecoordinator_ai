const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

function getRootVersion() {
  const versionFile = path.resolve(__dirname, '..', 'VERSION');
  if (fs.existsSync(versionFile)) {
    const content = fs.readFileSync(versionFile, 'utf8').trim();
    if (content) return content;
  }
  const pkgFile = path.resolve(__dirname, '..', 'client', 'package.json');
  if (fs.existsSync(pkgFile)) {
    try {
      const pkg = JSON.parse(fs.readFileSync(pkgFile, 'utf8'));
      if (pkg.version) return pkg.version.trim();
    } catch {
      // fallback
    }
  }
  return '0.0.0';
}

function getExistingTags() {
  try {
    const output = execSync('git tag -l', { encoding: 'utf8' });
    return output.split('\n').map(t => t.trim()).filter(Boolean);
  } catch {
    return [];
  }
}

function determineVersion(eventName, ref, overrideVersion, customTags, customRootVersion) {
  // 1. Manual override from workflow_dispatch
  if (overrideVersion && overrideVersion.trim()) {
    const cleanVer = overrideVersion.trim().replace(/^v/, '');
    const isPrerelease = cleanVer.includes('alpha') || cleanVer.includes('beta');
    return {
      version: cleanVer,
      tag: `v${cleanVer}`,
      isPrerelease: isPrerelease ? 'true' : 'false'
    };
  }

  // 2. Explicit git tag push
  if (ref && ref.startsWith('refs/tags/')) {
    const rawTag = ref.replace('refs/tags/', '');
    const cleanVer = rawTag.replace(/^v/, '');
    const isPrerelease = cleanVer.includes('alpha') || cleanVer.includes('beta');
    return {
      version: cleanVer,
      tag: rawTag.startsWith('v') ? rawTag : `v${rawTag}`,
      isPrerelease: isPrerelease ? 'true' : 'false'
    };
  }

  // 3. Schedule event -> Daily Alpha on develop
  if (eventName === 'schedule') {
    const now = new Date();
    const yyyy = now.getUTCFullYear();
    const mm = String(now.getUTCMonth() + 1).padStart(2, '0');
    const dd = String(now.getUTCDate()).padStart(2, '0');
    const dateStr = `${yyyy}${mm}${dd}`;
    const version = `0.0.0-alpha.${dateStr}`;
    return {
      version,
      tag: `v${version}`,
      isPrerelease: 'true'
    };
  }

  const existingTags = customTags !== undefined ? customTags : getExistingTags();

  // 4. Push to release branch (e.g. refs/heads/release/v1.0.0, release/v1.0.0, release/1.0.0, release/v1.0)
  if (ref && (ref.startsWith('refs/heads/release/') || ref.startsWith('release/'))) {
    const branchName = ref.replace(/^refs\/heads\//, '').replace(/^release\//, '');
    const base = branchName.replace(/^v/, '');
    const parts = base.split('.');
    while (parts.length < 3) {
      parts.push('0');
    }
    const baseVersion = parts.slice(0, 3).join('.'); // e.g. "1.0.0"

    // Find highest beta tag: e.g. v1.0.0-beta.1, v1.0.0-beta.2
    const betaRegex = new RegExp(`^v?${baseVersion.replace(/\./g, '\\.')}-beta\\.(\\d+)$`);
    let maxBeta = 0;
    for (const tag of existingTags) {
      const match = tag.match(betaRegex);
      if (match) {
        const num = parseInt(match[1], 10);
        if (num > maxBeta) maxBeta = num;
      }
    }
    const nextBeta = maxBeta + 1;
    const version = `${baseVersion}-beta.${nextBeta}`;
    return {
      version,
      tag: `v${version}`,
      isPrerelease: 'true'
    };
  }

  // 5. Push to main or default -> Official Production Release
  const rootVer = customRootVersion !== undefined ? customRootVersion : getRootVersion();
  const rootParts = rootVer.replace(/^v/, '').split('.');
  const major = rootParts[0] || '0';
  const minor = rootParts[1] || '0';
  const prefix = `${major}.${minor}`;

  // Find highest official patch tag: e.g. v1.0.0, v1.0.1 (strict numbers, no alpha/beta)
  const officialRegex = new RegExp(`^v?${major}\\.${minor}\\.(\\d+)$`);
  let maxPatch = -1;
  for (const tag of existingTags) {
    const match = tag.match(officialRegex);
    if (match) {
      const num = parseInt(match[1], 10);
      if (num > maxPatch) maxPatch = num;
    }
  }

  const nextPatch = maxPatch === -1 ? 0 : maxPatch + 1;
  const version = `${prefix}.${nextPatch}`;
  return {
    version,
    tag: `v${version}`,
    isPrerelease: 'false'
  };
}

function main() {
  const eventName = process.env.GITHUB_EVENT_NAME || '';
  const ref = process.env.GITHUB_REF || '';
  const overrideVersion = process.env.INPUT_VERSION || process.env.VERSION || '';

  const result = determineVersion(eventName, ref, overrideVersion);

  console.log(`Calculated Release:`);
  console.log(`  Version:       ${result.version}`);
  console.log(`  Tag:           ${result.tag}`);
  console.log(`  Is Prerelease: ${result.isPrerelease}`);

  const githubOutput = process.env.GITHUB_OUTPUT;
  if (githubOutput) {
    fs.appendFileSync(githubOutput, `version=${result.version}\n`);
    fs.appendFileSync(githubOutput, `tag=${result.tag}\n`);
    fs.appendFileSync(githubOutput, `is_prerelease=${result.isPrerelease}\n`);
  }
}

if (require.main === module) {
  main();
}

module.exports = {
  getRootVersion,
  getExistingTags,
  determineVersion
};
