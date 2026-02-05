import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
  return handleProxy(request);
}

export async function POST(request: NextRequest) {
  return handleProxy(request);
}

export async function PUT(request: NextRequest) {
  return handleProxy(request);
}

export async function DELETE(request: NextRequest) {
  return handleProxy(request);
}

export async function PATCH(request: NextRequest) {
  return handleProxy(request);
}

async function handleProxy(request: NextRequest) {
  const backendUrl = process.env.BACKEND_URL || 'http://3.37.61.22:8080';
  
  // Extract path and query from the original URL
  const url = new URL(request.url);
  const path = url.pathname.replace('/api/proxy', '');
  const queryString = url.search;
  
  const targetUrl = `${backendUrl}${path}${queryString}`;
  
  try {
    const headers: HeadersInit = {};
    
    // Forward relevant headers
    const forwardHeaders = ['content-type', 'authorization', 'cookie'];
    forwardHeaders.forEach(header => {
      const value = request.headers.get(header);
      if (value) {
        headers[header] = value;
      }
    });
    
    // Get body if exists
    let body = undefined;
    if (request.method !== 'GET' && request.method !== 'HEAD') {
      body = await request.text();
    }
    
    const response = await fetch(targetUrl, {
      method: request.method,
      headers,
      body,
    });
    
    const data = await response.text();
    
    // Forward response headers
    const responseHeaders = new Headers();
    response.headers.forEach((value, key) => {
      // Skip some headers that shouldn't be forwarded
      if (!['content-encoding', 'transfer-encoding'].includes(key.toLowerCase())) {
        responseHeaders.set(key, value);
      }
    });
    
    return new NextResponse(data, {
      status: response.status,
      headers: responseHeaders,
    });
  } catch (error) {
    console.error('Proxy error:', error);
    return NextResponse.json(
      { error: 'Proxy request failed' },
      { status: 500 }
    );
  }
}