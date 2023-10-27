export const log_levels = {
    "trace": 0,
    "debug": 1,
    "info": 2,
    "warn": 3,
    "error": 4,
    "fatal": 5
};

// create a value -> key dict
const levels = {};
for (const [key, value] of Object.entries(log_levels)) {
    levels[value] = key.toUpperCase();
}

// console.log("Logging levels: ", log_levels, "corresponding value dict: ", levels);

// Select logging level
const _log_level = log_levels.debug;

export function logLevel(log_level, ...args) {
    if (log_level >= _log_level) {
        console.log(levels[log_level], ...args);
    }
}

export function trace(...args) {
    logLevel(log_levels.trace, ...args);
}

export function debug(...args) {
    logLevel(log_levels.debug, ...args);
}

export function info(...args) {
    logLevel(log_levels.info, ...args);
}

export function warn(...args) {
    logLevel(log_levels.warn, ...args);
}

export function error(...args) {
    logLevel(log_levels.error, ...args);
}

export function fatal(...args) {
    logLevel(log_levels.fatal, ...args);
}