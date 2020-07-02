package controllers

import (
	"github.com/gin-gonic/gin"
)

func SetUpRoutes(router *gin.Engine) {
	router.GET("/ping", pingHandler)
	router.POST("/start", startHandler)
	router.GET("/result", resultHandler)
}
