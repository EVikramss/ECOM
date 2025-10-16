import pako from 'pako';

export function compressString(input) {
    const encoder = new TextEncoder();
    const encodedData = encoder.encode(input);
    const compressedData = pako.deflate(encodedData);
    const base64 = btoa(String.fromCharCode(...compressedData));
    return base64;
}

export function decompressString(base64) {
    const binaryString = atob(base64);
    const charData = Uint8Array.from(binaryString, c => c.charCodeAt(0));
    const decompressedData = pako.inflate(charData);
    const decoder = new TextDecoder();
    return decoder.decode(decompressedData);
}

export function compressObject(input) {
    const encoder = new TextEncoder();
    const encodedData = encoder.encode(JSON.stringify(input));
    const compressedData = pako.deflate(encodedData);
    const base64 = btoa(String.fromCharCode(...compressedData));
    return base64;
}

export function decompressObject(base64) {
    const binaryString = atob(base64);
    const charData = Uint8Array.from(binaryString, c => c.charCodeAt(0));
    const decompressedData = pako.inflate(charData);
    const decoder = new TextDecoder();
    return JSON.parse(decoder.decode(decompressedData));
}