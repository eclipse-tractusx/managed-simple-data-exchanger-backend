import SHA256 from 'crypto-js/sha256';
const getHash = (input: string): string => {
  return SHA256(input).toString();
};

export default getHash;
