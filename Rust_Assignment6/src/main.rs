extern crate hyper;
extern crate futures;
extern crate serde_json;

use std::str;
use std::sync::Mutex;
use lazy_static::lazy_static;
use std::collections::HashMap;
use hyper::{Body, Request, Response, Server, Method, StatusCode, Client};
use hyper::service::{service_fn, make_service_fn};
use futures::TryStreamExt;
use std::net::SocketAddr;
use serde::{Serialize, Deserialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AddRequest {
    pub key: i64,
    pub value: String,
}

lazy_static! {
    static ref MAP: Mutex<HashMap<i64, String>> = Mutex::new(HashMap::new());
}

const PEERS: &'static [&'static str] = &["http://b01da80d4fd5.ngrok.io/add", "http://dfb8ea99d50d.ngrok.io/add", "http://e3c3c5689d6d.ngrok.io/add"];
const NUM_PEERS: usize = 3;

async fn inform_peers(body: Vec<u8>) {
    let body_str = String::from_utf8(body).unwrap();
    for i in 0..NUM_PEERS {
        let req = Request::builder()
            .method(Method::POST)
            .uri(PEERS[i])
            .header("content-type", "application/json")
            .body(Body::from(body_str.clone())).unwrap();
        let _res = Client::new().request(req).await;
        println!("Informed {}",PEERS[i]); 
    }
}

async fn not_found() -> Response<Body> {
    let mut res = Response::new(Body::from("Incorrect URL"));
    *res.status_mut() = StatusCode::NOT_FOUND;
    res
}

async fn handle_ping() -> Response<Body> {
    Response::new(Body::from("PONG"))
}

async fn handle_add(req: Request<Body>) -> Response<Body> {
    let f_body = req.into_body();
    let body = f_body.try_fold(Vec::new(), |mut data, chunk| async move {
        data.extend_from_slice(&chunk);
        Ok(data)
    }).await;
    let cbody = body.unwrap();
    let cbody2 = cbody.clone();
    let r: AddRequest = serde_json::from_slice(&cbody2).unwrap();
    
    if !MAP.lock().unwrap().contains_key(&r.key) {
        let cln = r.clone();
        MAP.lock().unwrap().insert(r.key, r.value);
        println!("Inserted {} - {}", cln.key, cln.value);
        inform_peers(cbody).await;
    } else {
        println!("Key {} already exists", r.key);
    }
    
    Response::new(Body::from(""))
}

async fn handle_list() -> Response<Body> {
    let mut ans: String = "{".to_string();
    for (key, val) in MAP.lock().unwrap().iter() {
        ans = format!("{}\n\"{}\":\"{}\"**", ans, key, val);
    }
    ans = ans.replace("**\n", ",\n");
    ans = ans.replace("**", "}");
    if ans == "{" {
        ans = "".to_string();
    }
    
    Response::builder()
        .header("Content-Type", "application/json")
        .body(Body::from(ans))
        .unwrap()
}

async fn router(req: Request<Body>) -> Result<Response<Body>, hyper::Error> {
    match(req.method(), req.uri().path()) {
        (&Method::GET, "/ping") => Ok(handle_ping().await),
        (&Method::POST, "/add") => Ok(handle_add(req).await),
        (&Method::GET, "/list") => Ok(handle_list().await),
        (_, _) => Ok(not_found().await)
    }
}

async fn run_server(addr: SocketAddr) {
    println!("Listening on http://{}", addr);
    let serve_future = Server::bind(&addr).serve(
        make_service_fn(|_| async {
            Ok::<_, hyper::Error>(service_fn(router))
        }
    ));
    
    if let Err(e) = serve_future.await {
        eprintln!("Server error: {}", e);
    }
}

#[tokio::main]
async fn main() {
    let addr = "127.0.0.1:8080".parse().unwrap();
    run_server(addr).await;
}

