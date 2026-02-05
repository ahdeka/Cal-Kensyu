import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleProxy(request, params.path);
}

export async function POST(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleProxy(request, params.path);
}

export async function PUT(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleProxy(request, params.path);
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleProxy(request, params.path);
}

export async function PATCH(
  request: NextRequest,
  { params }: { params: { path: string[] } }
) {
  return handleProxy(request, params.path);
}

async function handleProxy(request: NextRequest, pathArray: string[]) {
  const backendUrl = process.env.BACKEND_URL || 'http://13.125.237.133:8080';
  
  // Reconstruct the path
  const path = '/' + pathArray.join('/');
  
  // Extract query string
  const url = new URL(request.url);
  const queryString = url.search;
  
  const targetUrl = `${backendUrl}${path}${queryString}`;
  
  console.log('Proxying to:', targetUrl);
  
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
      credentials: 'include',
    });
    
    const contentType = response.headers.get('content-type');
    let data;
    
    if (contentType?.includes('application/json')) {
      data = await response.json();
    } else {
      data = await response.text();
    }
    
    // Forward response headers
    const responseHeaders = new Headers();
    response.headers.forEach((value, key) => {
      // Skip some headers that shouldn't be forwarded
      if (!['content-encoding', 'transfer-encoding', 'connection'].includes(key.toLowerCase())) {
        responseHeaders.set(key, value);
      }
    });
    
    return NextResponse.json(data, {
      status: response.status,
      headers: responseHeaders,
    });
  } catch (error) {
    console.error('Proxy error:', error);
    return NextResponse.json(
      { error: 'Proxy request failed', details: String(error) },
      { status: 500 }
    );
  }
}