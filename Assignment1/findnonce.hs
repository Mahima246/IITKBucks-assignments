import Crypto.Hash          (hash, SHA256 (..), Digest)
import Data.ByteString.UTF8 (fromString)
import Data.List            (isPrefixOf)

getSHA256Hash :: String -> String
getSHA256Hash inputData = do
    let byteString = fromString inputData
    let digest :: Digest SHA256
        digest = hash byteString
    show digest

isValid :: String -> Bool
isValid str = isPrefixOf "0000" str

findNonce :: String -> Integer -> Integer
findNonce input currentNonce
    | isValid hsh = currentNonce
    | otherwise = findNonce input (currentNonce+1)
    where hsh = getSHA256Hash $ input ++ show currentNonce

main = do
    putStrLn "Enter the data: "
    inputData <- getLine
    let nonce = findNonce inputData 1
    putStrLn $ "Nonce = " ++ show nonce
    putStrLn $ "SHA256(" ++ inputData ++ show nonce ++ ") = " ++ getSHA256Hash (inputData ++ show nonce)

