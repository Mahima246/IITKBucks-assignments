import Crypto.PubKey.RSA  (generate)
import Crypto.Store.PKCS8 (PrivateKeyFormat(PKCS8Format), writeKeyFile)
import Crypto.Store.X509  (writePubKeyFile)
import Data.X509          (PrivKey(PrivKeyRSA), PubKey(PubKeyRSA))

main :: IO ()
main = do
    (pubKey, privKey) <- generate (2048 `div` 8) 0x10001
    
    let xPrivKey = PrivKeyRSA privKey
    let xPubKey = PubKeyRSA pubKey
    
    writeKeyFile PKCS8Format "private.pem" [xPrivKey]
    writePubKeyFile "public.pem" [xPubKey]

    putStrLn "Done."