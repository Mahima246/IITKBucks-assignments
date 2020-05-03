# Nonce Generator

The program finds an integer nonce value such that the SHA 256 hash of the concatenaton of the entered data and the nonce value is less than `0x0000FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF`.

## Compiling
```
ghc -dynamic findnonce
```

## Running
```
./findnonce
```
