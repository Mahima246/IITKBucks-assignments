package controllers

import (
	"github.com/gin-gonic/gin"
)

type startRequest struct {
	Data string `json:"data"`
}

type resultType struct {
	Status string `json:"result"`
	Nonce  int64  `json:"nonce"`
}

var result resultType
var stopChannel chan bool

func pingHandler(c *gin.Context) {
	c.JSON(200, gin.H{
		"message": "pong",
	})
}

func startHandler(c *gin.Context) {
	var request startRequest
	err := c.BindJSON(&request)
	if err != nil {
		_ = c.AbortWithError(400, err)
		return
	}

	stopChannel = make(chan bool)
	result = resultType{
		Status: "searching",
		Nonce:  -1,
	}

	go startMining(request.Data, stopChannel)

	c.Status(200)
}

func resultHandler(c *gin.Context) {
	c.JSON(200, result)
}

func stopHandler(c *gin.Context) {
	close(stopChannel)
	result.Status = "stopped"
	c.Status(200)
}
