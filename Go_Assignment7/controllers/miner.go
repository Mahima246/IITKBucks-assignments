package controllers

import (
	"bytes"
	"crypto/sha256"
	"encoding/hex"
	"fmt"
)

var targetString = "0000000f00000000000000000000000000000000000000000000000000000000"
var target []byte

func init() {
	var err error
	target, err = hex.DecodeString(targetString)
	if err != nil {
		panic(err)
	}
}

func startMining(data string) {
	for i := int64(0); ; i++ {
		currentString := fmt.Sprintf("%s%d", data, i)
		hash := sha256.Sum256([]byte(currentString))
		if bytes.Compare(hash[:], target[:]) == -1 {
			result.Status = "found"
			result.Nonce = i
			return
		}
	}
}
