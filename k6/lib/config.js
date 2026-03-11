const DEFAULT_BASE_URL = 'https://dev-api.simsaimdang.shop';
const DEFAULT_TARGET_PATH = '/api/v1/documents';
const DEFAULT_RESULTS_DIR = 'k6/results';
const DEFAULT_TIMEOUT = '30s';
const DEFAULT_THINK_TIME_SECONDS = 1;

function parseTokenList(raw) {
  return raw
    .split(/[,\n]/)
    .map((token) => token.trim())
    .filter((token) => token.length > 0);
}

function loadTokens() {
  const envTokens = __ENV.ACCESS_TOKENS || __ENV.ACCESS_TOKEN || '';
  const parsedEnvTokens = parseTokenList(envTokens);
  if (parsedEnvTokens.length > 0) {
    return parsedEnvTokens;
  }

  if (__ENV.ACCESS_TOKENS_FILE) {
    const fileContent = open(__ENV.ACCESS_TOKENS_FILE);
    const parsedFileTokens = parseTokenList(fileContent);
    if (parsedFileTokens.length > 0) {
      return parsedFileTokens;
    }
  }

  return [];
}

export const BASE_URL = (__ENV.BASE_URL || DEFAULT_BASE_URL).replace(/\/$/, '');
export const TARGET_PATH = __ENV.TARGET_PATH || DEFAULT_TARGET_PATH;
export const REQUEST_TIMEOUT = __ENV.REQUEST_TIMEOUT || DEFAULT_TIMEOUT;
export const RESULTS_DIR = __ENV.RESULTS_DIR || DEFAULT_RESULTS_DIR;
export const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || DEFAULT_THINK_TIME_SECONDS);
export const FOLDER_ID = Number(__ENV.FOLDER_ID || 0);
export const TOKENS = loadTokens();
export const RUN_LABEL = (__ENV.RUN_LABEL || 'manual').trim() || 'manual';
export const TARGET_P95_MS = Number(__ENV.TARGET_P95_MS || 500);
export const TARGET_P99_MS = Number(__ENV.TARGET_P99_MS || 1000);
export const TARGET_ERROR_RATE = Number(__ENV.TARGET_ERROR_RATE || 0.01);
